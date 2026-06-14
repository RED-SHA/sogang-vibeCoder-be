package com.kmedical.control;

import com.kmedical.adapter.PushAdapter;
import com.kmedical.domain.entity.StaffAssignment;
import com.kmedical.dto.staff.StaffAssignmentCreateRequestDTO;
import com.kmedical.dto.staff.StaffAssignmentDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
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
    private final Map<String, List<String>> itineraryAssignmentStore = new ConcurrentHashMap<>();

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

    public List<String> replaceAssignedStaffIds(String scheduleItemId, List<String> staffIds, String assignedBy) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(scheduleItemId, "scheduleItemId");
        List<String> safeStaffIds = staffIds == null ? Collections.emptyList() : new ArrayList<>(staffIds);
        itineraryAssignmentStore.put(scheduleItemId, Collections.unmodifiableList(safeStaffIds));
        AuditLogger.log("ITINERARY_STAFF_UPDATED", assignedBy, scheduleItemId, true,
                "staffIds=" + safeStaffIds);
        return new ArrayList<>(safeStaffIds);
    }

    public List<String> getAssignedStaffIds(String scheduleItemId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(scheduleItemId, "scheduleItemId");
        return new ArrayList<>(itineraryAssignmentStore.getOrDefault(scheduleItemId, Collections.emptyList()));
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
