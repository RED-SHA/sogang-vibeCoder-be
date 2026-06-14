package com.kmedical.http;

import com.kmedical.control.JourneyController;
import com.kmedical.domain.enums.EditItineraryErrorCode;
import com.kmedical.domain.enums.ScheduleItemStatus;
import com.kmedical.domain.enums.ScheduleItemType;
import com.kmedical.domain.enums.UserRoleName;
import com.kmedical.dto.journey.EditItineraryRequestDTO;
import com.kmedical.dto.journey.EditItineraryResponseDTO;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.journey.ScheduleItemDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * /api/journeys/** request handler.
 *
 * POST  /api/journeys                              UC-P08 journey create
 * GET   /api/journeys/{id}                         journey read
 * POST  /api/journeys/{id}/schedule                UC-A06 schedule item add
 * GET   /api/journeys/{id}/schedule                schedule list
 * PATCH /api/journeys/{id}/schedule/{scheduleId}   UC-ADM-07 edit itinerary
 */
public class JourneyHandler extends BaseHandler {

    private final JourneyController journeyController;

    public JourneyHandler(JourneyController journeyController) {
        this.journeyController = journeyController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("POST".equals(method) && "/api/journeys".equals(path)) {
            handleCreateJourney(ex);
        } else if ("GET".equals(method) && parts.length == 4) {
            handleGetJourney(ex, parts[3]);
        } else if ("POST".equals(method) && parts.length == 5 && "schedule".equals(parts[4])) {
            handleAddScheduleItem(ex, parts[3]);
        } else if ("GET".equals(method) && parts.length == 5 && "schedule".equals(parts[4])) {
            handleGetScheduleItems(ex, parts[3]);
        } else if ("PATCH".equals(method) && parts.length == 6 && "schedule".equals(parts[4])) {
            handleEditItinerary(ex, parts[3], parts[5]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleCreateJourney(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        PatientJourneyDTO dto = new PatientJourneyDTO();
        dto.setPatientId(body.get("patientId"));
        dto.setAgencyId(body.get("agencyId"));
        dto.setQuotationId(body.get("quotationId"));
        if (value(body, "arrivalDate") != null) {
            dto.setArrivalDate(LocalDate.parse(value(body, "arrivalDate")));
        }
        if (value(body, "departureDate") != null) {
            dto.setDepartureDate(LocalDate.parse(value(body, "departureDate")));
        }

        PatientJourneyDTO result = journeyController.createJourney(dto);
        sendJson(ex, 201, journeyToMap(result));
    }

    private void handleGetJourney(HttpExchange ex, String id) throws IOException {
        PatientJourneyDTO result = journeyController.getJourney(id);
        sendJson(ex, 200, journeyToMap(result));
    }

    private void handleAddScheduleItem(HttpExchange ex, String journeyId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        ScheduleItemDTO dto = new ScheduleItemDTO();
        dto.setPatientJourneyId(journeyId);
        dto.setTitle(value(body, "title"));
        dto.setLocationAddressEn(value(body, "locationAddressEn"));
        dto.setMemo(value(body, "memo"));
        if (value(body, "itemType") != null) {
            dto.setItemType(ScheduleItemType.valueOf(value(body, "itemType")));
        }
        if (value(body, "isCritical") != null) {
            dto.setIsCritical(Boolean.parseBoolean(value(body, "isCritical")));
        }
        if (value(body, "scheduledStartAt") != null) {
            dto.setScheduledStartAt(LocalDateTime.parse(value(body, "scheduledStartAt")));
        }
        if (value(body, "scheduledEndAt") != null) {
            dto.setScheduledEndAt(LocalDateTime.parse(value(body, "scheduledEndAt")));
        }
        if (value(body, "sortOrder") != null) {
            dto.setSortOrder(Integer.parseInt(value(body, "sortOrder")));
        }

        ScheduleItemDTO result = journeyController.addScheduleItem(dto);
        sendJson(ex, 201, scheduleItemToMap(result));
    }

    private void handleGetScheduleItems(HttpExchange ex, String journeyId) throws IOException {
        List<ScheduleItemDTO> list = journeyController.getScheduleItems(journeyId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ScheduleItemDTO item : list) items.add(scheduleItemToMap(item));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", items.size());
        response.put("items", items);
        sendJson(ex, 200, response);
    }

    private void handleEditItinerary(HttpExchange ex, String journeyId, String scheduleItemId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        EditItineraryRequestDTO request = new EditItineraryRequestDTO(
                value(body, "operatorId"),
                enumValue(UserRoleName.class, value(body, "operatorRole")),
                journeyId,
                scheduleItemId,
                integerValue(body, "expectedVersion"),
                booleanValue(body, "cancelRequested"),
                enumValue(ScheduleItemType.class, value(body, "itemType")),
                value(body, "title"),
                dateTimeValue(body, "scheduledStartAt"),
                dateTimeValue(body, "scheduledEndAt"),
                value(body, "locationAddressEn"),
                decimalValue(body, "locationCoordLat"),
                decimalValue(body, "locationCoordLng"),
                enumValue(ScheduleItemStatus.class, value(body, "status")),
                nullableBooleanValue(body, "isCritical"),
                value(body, "memo"),
                integerValue(body, "sortOrder"),
                staffIdsValue(body, "assignedStaffIds")
        );

        EditItineraryResponseDTO response = journeyController.editItinerary(request);
        sendJson(ex, statusFor(response), editItineraryToMap(response));
    }

    private int statusFor(EditItineraryResponseDTO response) {
        if (response.isSuccess() || response.isCancelled()) return 200;
        EditItineraryErrorCode code = response.getErrorCode();
        if (code == EditItineraryErrorCode.PERMISSION_DENIED) return 403;
        if (code == EditItineraryErrorCode.VALIDATION_FAILED) return 400;
        if (code == EditItineraryErrorCode.OPTIMISTIC_LOCK_CONFLICT) return 409;
        if (code == EditItineraryErrorCode.LOCK_HELD) return 423;
        return 500;
    }

    private Map<String, Object> journeyToMap(PatientJourneyDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("patientJourneyId", dto.getPatientJourneyId());
        m.put("patientId", dto.getPatientId());
        m.put("agencyId", dto.getAgencyId());
        m.put("quotationId", dto.getQuotationId());
        m.put("status", dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("arrivalDate", dto.getArrivalDate() != null ? dto.getArrivalDate().toString() : null);
        m.put("departureDate", dto.getDepartureDate() != null ? dto.getDepartureDate().toString() : null);
        m.put("createdAt", dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null);
        return m;
    }

    private Map<String, Object> scheduleItemToMap(ScheduleItemDTO dto) {
        if (dto == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scheduleItemId", dto.getScheduleItemId());
        m.put("patientJourneyId", dto.getPatientJourneyId());
        m.put("itemType", dto.getItemType() != null ? dto.getItemType().name() : null);
        m.put("title", dto.getTitle());
        m.put("scheduledStartAt", dto.getScheduledStartAt() != null ? dto.getScheduledStartAt().toString() : null);
        m.put("scheduledEndAt", dto.getScheduledEndAt() != null ? dto.getScheduledEndAt().toString() : null);
        m.put("locationAddressEn", dto.getLocationAddressEn());
        m.put("locationCoordLat", dto.getLocationCoordLat());
        m.put("locationCoordLng", dto.getLocationCoordLng());
        m.put("status", dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("isCritical", dto.getIsCritical());
        m.put("memo", dto.getMemo());
        m.put("sortOrder", dto.getSortOrder());
        m.put("version", dto.getVersion());
        return m;
    }

    private Map<String, Object> editItineraryToMap(EditItineraryResponseDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", dto.isSuccess());
        m.put("cancelled", dto.isCancelled());
        m.put("state", dto.getState() != null ? dto.getState().name() : null);
        m.put("updatedScheduleItem", scheduleItemToMap(dto.getUpdatedScheduleItem()));
        m.put("latestScheduleItem", scheduleItemToMap(dto.getLatestScheduleItem()));
        m.put("assignedStaffIds", dto.getAssignedStaffIds());
        m.put("degradedSync", dto.isDegradedSync());
        m.put("pushWarning", dto.isPushWarning());
        m.put("errorCode", dto.getErrorCode() != null ? dto.getErrorCode().name() : null);
        m.put("errorDetail", dto.getErrorDetail());
        m.put("lockHolderId", dto.getLockHolderId());
        return m;
    }

    private String value(Map<String, String> body, String name) {
        String v = body.get(name);
        if (v == null || "null".equals(v)) return null;
        return v;
    }

    private Integer integerValue(Map<String, String> body, String name) {
        String v = value(body, name);
        return v == null ? null : Integer.valueOf(v);
    }

    private BigDecimal decimalValue(Map<String, String> body, String name) {
        String v = value(body, name);
        return v == null ? null : new BigDecimal(v);
    }

    private LocalDateTime dateTimeValue(Map<String, String> body, String name) {
        String v = value(body, name);
        return v == null ? null : LocalDateTime.parse(v);
    }

    private boolean booleanValue(Map<String, String> body, String name) {
        String v = value(body, name);
        return v != null && Boolean.parseBoolean(v);
    }

    private Boolean nullableBooleanValue(Map<String, String> body, String name) {
        String v = value(body, name);
        return v == null ? null : Boolean.valueOf(v);
    }

    private List<String> staffIdsValue(Map<String, String> body, String name) {
        String v = value(body, name);
        if (v == null || v.isBlank()) return null;
        List<String> ids = new ArrayList<>();
        for (String id : Arrays.asList(v.split(","))) {
            String trimmed = id.trim();
            if (!trimmed.isEmpty()) ids.add(trimmed);
        }
        return ids;
    }

    private <T extends Enum<T>> T enumValue(Class<T> type, String value) {
        return value == null ? null : Enum.valueOf(type, value);
    }
}
