package com.kmedical.http;

import com.kmedical.control.JourneyController;
import com.kmedical.domain.enums.EditItineraryErrorCode;
import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.domain.enums.ScheduleItemType;
import com.kmedical.dto.journey.*;
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
 * PUT  /api/journeys/{id}/schedule/{sid} UC-ADM-07 일정 수정
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
        if (body.get("scheduledStartAt") != null) {
            dto.setScheduledStartAt(LocalDateTime.parse(body.get("scheduledStartAt")));
        }
        if (body.get("scheduledEndAt") != null) {
            dto.setScheduledEndAt(LocalDateTime.parse(body.get("scheduledEndAt")));
        }
        if (body.get("locationCoordLat") != null) {
            dto.setLocationCoordLat(new BigDecimal(body.get("locationCoordLat")));
        }
        if (body.get("locationCoordLng") != null) {
            dto.setLocationCoordLng(new BigDecimal(body.get("locationCoordLng")));
        }
        if (body.get("sortOrder") != null) {
            dto.setSortOrder(Integer.parseInt(body.get("sortOrder")));
        }
        if (body.get("assignedStaffIds") != null) {
            dto.setAssignedStaffIds(parseCsv(body.get("assignedStaffIds")));
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

    // ── UC-ADM-07: 일정 수정 ─────────────────────────────────────────────────
    private void handleEditItinerary(HttpExchange ex, String journeyId, String scheduleItemId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        ScheduleItemUpdateRequestDTO req = new ScheduleItemUpdateRequestDTO();
        req.setPatientJourneyId(journeyId);
        req.setScheduleItemId(scheduleItemId);
        req.setOperatorId(body.get("operatorId"));
        if (body.get("expectedVersion") != null) {
            req.setExpectedVersion(Integer.parseInt(body.get("expectedVersion")));
        }
        if (body.get("cancelRequested") != null) {
            req.setCancelRequested(Boolean.parseBoolean(body.get("cancelRequested")));
        }
        if (body.get("itemType") != null) {
            req.setItemType(ScheduleItemType.valueOf(body.get("itemType")));
        }
        if (body.get("title") != null) {
            req.setTitle(body.get("title"));
        }
        if (body.get("scheduledStartAt") != null) {
            req.setScheduledStartAt(LocalDateTime.parse(body.get("scheduledStartAt")));
        }
        if (body.get("scheduledEndAt") != null) {
            req.setScheduledEndAt(LocalDateTime.parse(body.get("scheduledEndAt")));
        }
        if (body.get("locationAddressEn") != null) {
            req.setLocationAddressEn(body.get("locationAddressEn"));
        }
        if (body.get("locationCoordLat") != null) {
            req.setLocationCoordLat(new BigDecimal(body.get("locationCoordLat")));
        }
        if (body.get("locationCoordLng") != null) {
            req.setLocationCoordLng(new BigDecimal(body.get("locationCoordLng")));
        }
        if (body.get("status") != null) {
            req.setStatus(ScheduleItemStatus.valueOf(body.get("status")));
        }
        if (body.get("isCritical") != null) {
            req.setIsCritical(Boolean.parseBoolean(body.get("isCritical")));
        }
        if (body.get("memo") != null) {
            req.setMemo(body.get("memo"));
        }
        if (body.get("sortOrder") != null) {
            req.setSortOrder(Integer.parseInt(body.get("sortOrder")));
        }
        if (body.get("assignedStaffIds") != null) {
            req.setAssignedStaffIds(parseCsv(body.get("assignedStaffIds")));
        }
        if (body.get("patientMagicLinkEndpoint") != null) {
            req.setPatientMagicLinkEndpoint(body.get("patientMagicLinkEndpoint"));
        }

        EditItineraryResponseDTO response = journeyController.editItinerary(req);
        sendJson(ex, statusFor(response), editItineraryToMap(response));
    }

    // ── DTO → Map 변환 ─────────────────────────────────────────────────────────

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
        m.put("scheduledStartAt",   dto.getScheduledStartAt() != null ? dto.getScheduledStartAt().toString() : null);
        m.put("scheduledEndAt",     dto.getScheduledEndAt() != null ? dto.getScheduledEndAt().toString() : null);
        m.put("locationAddressEn",  dto.getLocationAddressEn());
        m.put("locationCoordLat",   dto.getLocationCoordLat());
        m.put("locationCoordLng",   dto.getLocationCoordLng());
        m.put("status",             dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("isCritical",         dto.getIsCritical());
        m.put("memo",               dto.getMemo());
        m.put("sortOrder",          dto.getSortOrder());
        m.put("version",            dto.getVersion());
        m.put("assignedStaffIds",   dto.getAssignedStaffIds());
        return m;
    }

    private Map<String, Object> editItineraryToMap(EditItineraryResponseDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", dto.isSuccess());
        m.put("cancelled", dto.isCancelled());
        m.put("degradedSync", dto.isDegradedSync());
        m.put("pushWarning", dto.isPushWarning());
        m.put("status", dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("errorCode", dto.getErrorCode() != null ? dto.getErrorCode().name() : null);
        m.put("errorDetail", dto.getErrorDetail());
        m.put("fieldErrors", dto.getFieldErrors());
        m.put("updatedScheduleItem", dto.getUpdatedScheduleItem() != null
                ? scheduleItemToMap(dto.getUpdatedScheduleItem())
                : null);
        m.put("latestScheduleItem", dto.getLatestScheduleItem() != null
                ? scheduleItemToMap(dto.getLatestScheduleItem())
                : null);
        m.put("submittedScheduleItem", dto.getSubmittedScheduleItem() != null
                ? scheduleItemToMap(dto.getSubmittedScheduleItem())
                : null);
        m.put("assignedStaffIds", dto.getAssignedStaffIds());
        return m;
    }

    private int statusFor(EditItineraryResponseDTO response) {
        if (response.isSuccess()) return response.isDegradedSync() ? 202 : 200;
        if (response.isCancelled()) return 200;
        if (response.getErrorCode() == EditItineraryErrorCode.PERMISSION_DENIED) return 403;
        if (response.getErrorCode() == EditItineraryErrorCode.LOCK_HELD) return 423;
        if (response.getErrorCode() == EditItineraryErrorCode.OPTIMISTIC_LOCK_CONFLICT) return 409;
        if (response.getErrorCode() == EditItineraryErrorCode.VALIDATION_FAILED) return 400;
        return 409;
    }

    private List<String> parseCsv(String value) {
        List<String> result = new ArrayList<>();
        if (value == null || value.isBlank()) return result;
        for (String token : value.split(",")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }
}
