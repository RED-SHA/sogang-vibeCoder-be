package com.kmedical.http;

import com.kmedical.control.JourneyController;
import com.kmedical.domain.enums.ScheduleItemType;
import com.kmedical.dto.journey.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDate;
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
        return m;
    }
}
