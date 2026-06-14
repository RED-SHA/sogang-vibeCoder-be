package com.kmedical.http;

import com.kmedical.control.JourneyController;
import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.domain.enums.ScheduleItemType;
import com.kmedical.domain.enums.StaffRole;
import com.kmedical.dto.journey.*;
import com.kmedical.dto.staff.StaffAssignmentCreateRequestDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * /api/journeys/** 요청 처리
 *
 * POST /api/journeys                  UC-P08 여정 생성
 * GET  /api/journeys/{id}             여정 조회
 * POST /api/journeys/{id}/schedule    UC-A06 일정 항목 추가
 * GET  /api/journeys/{id}/schedule    일정 목록 조회
 */
public class JourneyHandler extends BaseHandler {

    private final JourneyController journeyController;

    public JourneyHandler(JourneyController journeyController) {
        this.journeyController = journeyController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        // parts: ["", "api", "journeys", {id}, "schedule"]
        String[] parts = path.split("/");

        if ("POST".equals(method) && "/api/journeys".equals(path)) {
            handleCreateJourney(ex);
        } else if ("GET".equals(method) && parts.length == 4) {
            handleGetJourney(ex, parts[3]);
        } else if ("POST".equals(method) && parts.length == 5 && "schedule".equals(parts[4])) {
            handleAddScheduleItem(ex, parts[3]);
        } else if ("GET".equals(method) && parts.length == 5 && "schedule".equals(parts[4])) {
            handleGetScheduleItems(ex, parts[3]);
        } else if ("PUT".equals(method) && parts.length == 6 && "schedule".equals(parts[4])) {
            handleEditItinerary(ex, parts[3], parts[5]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    // ── UC-P08: 여정 생성 ─────────────────────────────────────────────────────
    private void handleCreateJourney(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        PatientJourneyDTO dto = new PatientJourneyDTO();
        dto.setPatientId(body.get("patientId"));
        dto.setAgencyId(body.get("agencyId"));
        dto.setQuotationId(body.get("quotationId"));
        if (body.get("arrivalDate") != null) {
            dto.setArrivalDate(LocalDate.parse(body.get("arrivalDate")));
        }
        if (body.get("departureDate") != null) {
            dto.setDepartureDate(LocalDate.parse(body.get("departureDate")));
        }

        PatientJourneyDTO result = journeyController.createJourney(dto);
        sendJson(ex, 201, journeyToMap(result));
    }

    // ── 여정 단건 조회 ─────────────────────────────────────────────────────────
    private void handleGetJourney(HttpExchange ex, String id) throws IOException {
        PatientJourneyDTO result = journeyController.getJourney(id);
        sendJson(ex, 200, journeyToMap(result));
    }

    // ── UC-A06: 일정 항목 추가 ─────────────────────────────────────────────────
    private void handleAddScheduleItem(HttpExchange ex, String journeyId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        ScheduleItemDTO dto = new ScheduleItemDTO();
        dto.setPatientJourneyId(journeyId);
        dto.setTitle(body.get("title"));
        dto.setLocationAddressEn(body.get("locationAddressEn"));
        dto.setMemo(body.get("memo"));
        if (body.get("itemType") != null) {
            dto.setItemType(ScheduleItemType.valueOf(body.get("itemType")));
        }
        if (body.get("isCritical") != null) {
            dto.setIsCritical(Boolean.parseBoolean(body.get("isCritical")));
        }

        ScheduleItemDTO result = journeyController.addScheduleItem(dto);
        sendJson(ex, 201, scheduleItemToMap(result));
    }

    // ── 일정 목록 조회 ─────────────────────────────────────────────────────────
    private void handleGetScheduleItems(HttpExchange ex, String journeyId) throws IOException {
        List<ScheduleItemDTO> list = journeyController.getScheduleItems(journeyId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ScheduleItemDTO item : list) items.add(scheduleItemToMap(item));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", items.size());
        response.put("items", items);
        sendJson(ex, 200, response);
    }

    // ── DTO → Map 변환 ─────────────────────────────────────────────────────────

    // UC-ADM-07: Edit Itinerary
    private void handleEditItinerary(HttpExchange ex, String journeyId, String scheduleItemId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        EditItineraryRequestDTO request = new EditItineraryRequestDTO();
        request.setPatientJourneyId(journeyId);
        request.setScheduleItemId(scheduleItemId);
        request.setOperatorId(body.get("operatorId"));
        if (body.get("expectedVersion") != null) {
            request.setExpectedVersion(Integer.parseInt(body.get("expectedVersion")));
        }
        if (body.get("cancelRequested") != null) {
            request.setCancelRequested(Boolean.parseBoolean(body.get("cancelRequested")));
        }
        request.setUpdate(toScheduleUpdate(scheduleItemId, body));
        request.setStaffAssignments(toStaffAssignments(scheduleItemId, body));

        EditItineraryResponseDTO result = journeyController.editItinerary(request);
        sendJson(ex, editItineraryHttpStatus(result), editItineraryToMap(result));
    }

    private Map<String, Object> journeyToMap(PatientJourneyDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("patientJourneyId",   dto.getPatientJourneyId());
        m.put("patientId",          dto.getPatientId());
        m.put("agencyId",           dto.getAgencyId());
        m.put("quotationId",        dto.getQuotationId());
        m.put("status",             dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("arrivalDate",        dto.getArrivalDate() != null ? dto.getArrivalDate().toString() : null);
        m.put("departureDate",      dto.getDepartureDate() != null ? dto.getDepartureDate().toString() : null);
        m.put("createdAt",          dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null);
        return m;
    }

    private Map<String, Object> scheduleItemToMap(ScheduleItemDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scheduleItemId",     dto.getScheduleItemId());
        m.put("patientJourneyId",   dto.getPatientJourneyId());
        m.put("itemType",           dto.getItemType() != null ? dto.getItemType().name() : null);
        m.put("title",              dto.getTitle());
        m.put("locationAddressEn",  dto.getLocationAddressEn());
        m.put("status",             dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("isCritical",         dto.getIsCritical());
        m.put("memo",               dto.getMemo());
        m.put("scheduledStartAt",    dto.getScheduledStartAt() != null ? dto.getScheduledStartAt().toString() : null);
        m.put("scheduledEndAt",      dto.getScheduledEndAt() != null ? dto.getScheduledEndAt().toString() : null);
        m.put("locationCoordLat",    dto.getLocationCoordLat());
        m.put("locationCoordLng",    dto.getLocationCoordLng());
        m.put("sortOrder",          dto.getSortOrder());
        m.put("version",            dto.getVersion());
        return m;
    }

    private ScheduleItemUpdateRequestDTO toScheduleUpdate(String scheduleItemId, Map<String, String> body) {
        ScheduleItemUpdateRequestDTO dto = new ScheduleItemUpdateRequestDTO();
        dto.setScheduleItemId(scheduleItemId);
        if (body.get("itemType") != null) dto.setItemType(ScheduleItemType.valueOf(body.get("itemType")));
        if (body.get("title") != null) dto.setTitle(body.get("title"));
        if (body.get("scheduledStartAt") != null) dto.setScheduledStartAt(LocalDateTime.parse(body.get("scheduledStartAt")));
        if (body.get("scheduledEndAt") != null) dto.setScheduledEndAt(LocalDateTime.parse(body.get("scheduledEndAt")));
        if (body.get("locationAddressEn") != null) dto.setLocationAddressEn(body.get("locationAddressEn"));
        if (body.get("locationCoordLat") != null) dto.setLocationCoordLat(new BigDecimal(body.get("locationCoordLat")));
        if (body.get("locationCoordLng") != null) dto.setLocationCoordLng(new BigDecimal(body.get("locationCoordLng")));
        if (body.get("status") != null) dto.setStatus(ScheduleItemStatus.valueOf(body.get("status")));
        if (body.get("isCritical") != null) dto.setIsCritical(Boolean.parseBoolean(body.get("isCritical")));
        if (body.get("memo") != null) dto.setMemo(body.get("memo"));
        return dto;
    }

    private List<StaffAssignmentCreateRequestDTO> toStaffAssignments(String scheduleItemId, Map<String, String> body) {
        List<StaffAssignmentCreateRequestDTO> result = new ArrayList<>();
        addStaffAssignment(result, scheduleItemId, body.get("chauffeurId"), StaffRole.CHAUFFEUR, body.get("operatorId"));
        addStaffAssignment(result, scheduleItemId, body.get("interpreterId"), StaffRole.INTERPRETER, body.get("operatorId"));

        String assignedStaffIds = body.get("assignedStaffIds");
        if (result.isEmpty() && assignedStaffIds != null && !assignedStaffIds.isBlank()) {
            String[] ids = assignedStaffIds.split(",");
            for (int i = 0; i < ids.length; i++) {
                StaffRole role = i == 0 ? StaffRole.CHAUFFEUR : StaffRole.INTERPRETER;
                addStaffAssignment(result, scheduleItemId, ids[i].trim(), role, body.get("operatorId"));
            }
        }
        return result.isEmpty() && !body.containsKey("assignedStaffIds")
                && !body.containsKey("chauffeurId") && !body.containsKey("interpreterId")
                ? null : result;
    }

    private void addStaffAssignment(List<StaffAssignmentCreateRequestDTO> result,
                                    String scheduleItemId,
                                    String staffId,
                                    StaffRole role,
                                    String operatorId) {
        if (staffId == null || staffId.isBlank()) return;
        StaffAssignmentCreateRequestDTO dto = new StaffAssignmentCreateRequestDTO();
        dto.setScheduleItemId(scheduleItemId);
        dto.setStaffId(staffId);
        dto.setStaffRole(role);
        dto.setAssignedBy(operatorId);
        result.add(dto);
    }

    private int editItineraryHttpStatus(EditItineraryResponseDTO result) {
        if (result.isSuccess()) return 200;
        if (result.getErrorCode() == null) return 400;
        switch (result.getErrorCode()) {
            case PERMISSION_DENIED: return 403;
            case LOCK_HELD: return 423;
            case OPTIMISTIC_LOCK_CONFLICT: return 409;
            case VALIDATION_FAILED: return 400;
            case EDIT_CANCELLED: return 200;
            default: return 200;
        }
    }

    private Map<String, Object> editItineraryToMap(EditItineraryResponseDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", dto.isSuccess());
        m.put("status", dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("degradedSync", dto.isDegradedSync());
        m.put("pushWarning", dto.isPushWarning());
        m.put("errorCode", dto.getErrorCode() != null ? dto.getErrorCode().name() : null);
        m.put("errorDetail", dto.getErrorDetail());
        m.put("lockHolderId", dto.getLockHolderId());
        m.put("updatedScheduleItem", dto.getUpdatedScheduleItem() != null ? scheduleItemToMap(dto.getUpdatedScheduleItem()) : null);
        m.put("latestScheduleItem", dto.getLatestScheduleItem() != null ? scheduleItemToMap(dto.getLatestScheduleItem()) : null);
        m.put("submittedScheduleItem", dto.getSubmittedScheduleItem() != null ? scheduleItemToMap(dto.getSubmittedScheduleItem()) : null);
        List<Map<String, Object>> assignments = new ArrayList<>();
        for (com.kmedical.dto.staff.StaffAssignmentDTO assignment : dto.getAssignments()) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("assignmentId", assignment.getAssignmentId());
            a.put("scheduleItemId", assignment.getScheduleItemId());
            a.put("staffId", assignment.getStaffId());
            a.put("staffRole", assignment.getStaffRole() != null ? assignment.getStaffRole().name() : null);
            a.put("assignedBy", assignment.getAssignedBy());
            a.put("assignedAt", assignment.getAssignedAt() != null ? assignment.getAssignedAt().toString() : null);
            assignments.add(a);
        }
        m.put("assignments", assignments);
        return m;
    }
}
