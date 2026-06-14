package com.kmedical.control;

import com.kmedical.adapter.PushAdapter;
import com.kmedical.domain.entity.StaffAssignment;
import com.kmedical.dto.staff.StaffAssignmentCreateRequestDTO;
import com.kmedical.dto.staff.StaffAssignmentDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * SRV-C10 — StaffAssignmentController
 * 책임: 일정별 스태프 배정 (CHAUFFEUR·INTERPRETER 각 1명 제한), 알림 발송.
 * UC: UC-A07, UC-S03
 * 제약: 동일 ScheduleItem에 CHAUFFEUR 1명, INTERPRETER 1명만 배정 가능
 * NFR 적용: ConcurrentHashMap, AuditLogger(STAFF_ASSIGNED)
 */
public class StaffAssignmentController {

    private final PushAdapter pushAdapter;
    private final Map<String, StaffAssignment> assignmentStore = new ConcurrentHashMap<>();

    public StaffAssignmentController(PushAdapter pushAdapter) {
        this.pushAdapter = pushAdapter;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("StaffAssignmentController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 일정 항목에 스태프를 배정한다.
     * System Response: 중복 역할 배정 확인 → StaffAssignment 저장 → Push 알림 발송
     * NFR-LOG: STAFF_ASSIGNED 감사 로그
     */
    public StaffAssignmentDTO assignStaff(StaffAssignmentCreateRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "StaffAssignmentCreateRequestDTO");
        ValidationUtil.requireNotBlank(request.getScheduleItemId(), "scheduleItemId");
        ValidationUtil.requireNotBlank(request.getStaffId(), "staffId");
        ValidationUtil.requireNotNull(request.getStaffRole(), "staffRole");

        try {
            boolean duplicate = assignmentStore.values().stream()
                    .anyMatch(a -> a.getScheduleItemId().equals(request.getScheduleItemId())
                            && a.getStaffRole() == request.getStaffRole());
            if (duplicate) {
                throw new IllegalStateException(
                        "A " + request.getStaffRole() + " is already assigned to schedule item: "
                                + request.getScheduleItemId());
            }

            StaffAssignment assignment = new StaffAssignment();
            assignment.setAssignmentId(UUID.randomUUID().toString());
            assignment.setScheduleItemId(request.getScheduleItemId());
            assignment.setStaffId(request.getStaffId());
            assignment.setStaffRole(request.getStaffRole());
            assignment.setAssignedBy(request.getAssignedBy());
            assignment.setAssignedAt(LocalDateTime.now());

            assignmentStore.put(assignment.getAssignmentId(), assignment);

            boolean sent = pushAdapter.sendPush(
                    request.getStaffId(),
                    "New Assignment",
                    "You have been assigned to a schedule item."
            );
            if (sent) assignment.setNotificationSentAt(LocalDateTime.now());

            AuditLogger.log("STAFF_ASSIGNED", request.getAssignedBy(), request.getStaffId(), true,
                    "scheduleItemId=" + request.getScheduleItemId() + " role=" + request.getStaffRole());
            return toDTO(assignment);

        } catch (Exception e) {
            AuditLogger.log("STAFF_ASSIGNED", request.getAssignedBy(), request.getStaffId(), false, e.getMessage());
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) throw e;
            throw new IllegalStateException("Staff assignment failed: " + e.getMessage());
        }
    }

    /**
     * 일정 항목의 배정 목록을 조회한다.
     */
    public List<StaffAssignmentDTO> getAssignmentsByScheduleItem(String scheduleItemId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(scheduleItemId, "scheduleItemId");
        List<StaffAssignmentDTO> result = new ArrayList<>();
        for (StaffAssignment a : assignmentStore.values()) {
            if (a.getScheduleItemId().equals(scheduleItemId)) result.add(toDTO(a));
        }
        return result;
    }

    /**
     * UC-ADM-07 Step 9: replace assigned staff inside the itinerary edit flow.
     * Push notification is intentionally left to Step 12 of UC-ADM-07.
     */
    public synchronized List<StaffAssignmentDTO> replaceAssignmentsForScheduleItem(
            String scheduleItemId,
            List<StaffAssignmentCreateRequestDTO> requests,
            String operatorId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(scheduleItemId, "scheduleItemId");
        if (requests == null) return getAssignmentsByScheduleItem(scheduleItemId);

        Set<Object> roles = new HashSet<>();
        for (StaffAssignmentCreateRequestDTO request : requests) {
            ValidationUtil.requireNotNull(request, "StaffAssignmentCreateRequestDTO");
            ValidationUtil.requireNotBlank(request.getStaffId(), "staffId");
            ValidationUtil.requireNotNull(request.getStaffRole(), "staffRole");
            if (!roles.add(request.getStaffRole())) {
                throw new IllegalStateException("Duplicate staffRole in itinerary edit: " + request.getStaffRole());
            }
        }

        assignmentStore.entrySet().removeIf(e -> scheduleItemId.equals(e.getValue().getScheduleItemId()));
        List<StaffAssignmentDTO> result = new ArrayList<>();
        for (StaffAssignmentCreateRequestDTO request : requests) {
            StaffAssignment assignment = new StaffAssignment();
            assignment.setAssignmentId(UUID.randomUUID().toString());
            assignment.setScheduleItemId(scheduleItemId);
            assignment.setStaffId(request.getStaffId());
            assignment.setStaffRole(request.getStaffRole());
            assignment.setAssignedBy(operatorId);
            assignment.setAssignedAt(LocalDateTime.now());
            assignmentStore.put(assignment.getAssignmentId(), assignment);
            result.add(toDTO(assignment));
        }

        AuditLogger.log("ITINERARY_STAFF_REPLACED", operatorId, scheduleItemId, true,
                "assignmentCount=" + result.size());
        return result;
    }

    /**
     * 스태프의 배정 목록을 조회한다 (UC-S03 당일 업무 목록).
     */
    public List<StaffAssignmentDTO> getAssignmentsByStaff(String staffId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(staffId, "staffId");
        List<StaffAssignmentDTO> result = new ArrayList<>();
        for (StaffAssignment a : assignmentStore.values()) {
            if (a.getStaffId().equals(staffId)) result.add(toDTO(a));
        }
        return result;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private StaffAssignmentDTO toDTO(StaffAssignment a) {
        StaffAssignmentDTO dto = new StaffAssignmentDTO();
        dto.setAssignmentId(a.getAssignmentId());
        dto.setScheduleItemId(a.getScheduleItemId());
        dto.setStaffId(a.getStaffId());
        dto.setStaffRole(a.getStaffRole());
        dto.setAssignedBy(a.getAssignedBy());
        dto.setAssignedAt(a.getAssignedAt());
        dto.setNotificationSentAt(a.getNotificationSentAt());
        return dto;
    }
}
