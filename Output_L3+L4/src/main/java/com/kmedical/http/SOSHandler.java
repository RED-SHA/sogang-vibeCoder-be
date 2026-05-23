package com.kmedical.http;

import com.kmedical.control.SOSController;
import com.kmedical.dto.sos.SOSAlertDTO;
import com.kmedical.dto.sos.SOSAlertRequestDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * SRV-C14 SOSController HTTP 매핑
 *
 * POST /api/sos                긴급 호출 생성
 * PUT  /api/sos/{id}/resolve   긴급 호출 해결 처리
 * GET  /api/sos/unresolved     미해결 긴급 호출 목록
 */
public class SOSHandler extends BaseHandler {

    private final SOSController sosController;

    public SOSHandler(SOSController sosController) {
        this.sosController = sosController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("POST".equals(method) && "/api/sos".equals(path)) {
            handleTrigger(ex);
        } else if ("GET".equals(method) && "/api/sos/unresolved".equals(path)) {
            handleUnresolved(ex);
        } else if ("PUT".equals(method) && parts.length == 5 && "resolve".equals(parts[4])) {
            handleResolve(ex, parts[3]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleTrigger(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        SOSAlertRequestDTO req = new SOSAlertRequestDTO();
        req.setStaffId(body.get("staffId"));
        req.setPatientJourneyId(body.get("patientJourneyId"));
        req.setNotes(body.get("notes"));
        if (body.get("locationLat") != null) req.setLocationLat(new BigDecimal(body.get("locationLat")));
        if (body.get("locationLng") != null) req.setLocationLng(new BigDecimal(body.get("locationLng")));
        sendJson(ex, 201, sosToMap(sosController.triggerSOS(req)));
    }

    private void handleResolve(HttpExchange ex, String id) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        sendJson(ex, 200, sosToMap(sosController.resolveSOS(id, body.get("resolvedBy"), body.get("notes"))));
    }

    private void handleUnresolved(HttpExchange ex) throws IOException {
        List<SOSAlertDTO> list = sosController.getUnresolvedSOS();
        List<Map<String, Object>> items = new ArrayList<>();
        for (SOSAlertDTO s : list) items.add(sosToMap(s));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private Map<String, Object> sosToMap(SOSAlertDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("emergencyAlertId", dto.getEmergencyAlertId());
        m.put("staffId",          dto.getStaffId());
        m.put("patientJourneyId", dto.getPatientJourneyId());
        m.put("locationLat",      dto.getLocationLat());
        m.put("locationLng",      dto.getLocationLng());
        m.put("notes",            dto.getNotes());
        m.put("reportedAt",       dto.getReportedAt() != null ? dto.getReportedAt().toString() : null);
        m.put("resolvedAt",       dto.getResolvedAt() != null ? dto.getResolvedAt().toString() : null);
        m.put("resolvedBy",       dto.getResolvedBy());
        return m;
    }
}
