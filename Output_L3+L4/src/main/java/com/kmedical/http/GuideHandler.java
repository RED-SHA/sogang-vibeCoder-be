package com.kmedical.http;

import com.kmedical.control.GuideController;
import com.kmedical.dto.guide.GuideCreateRequestDTO;
import com.kmedical.dto.guide.GuideDeliveryRequestDTO;
import com.kmedical.dto.guide.RecoveryGuideDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * SRV-C16 GuideController HTTP 매핑
 *
 * POST /api/guides              회복 가이드 등록
 * POST /api/guides/deliver      환자에게 가이드 배포
 * GET  /api/guides/{id}         가이드 단건 조회
 * GET  /api/guides?patientId=   환자 배포 가이드 목록
 */
public class GuideHandler extends BaseHandler {

    private final GuideController guideController;

    public GuideHandler(GuideController guideController) {
        this.guideController = guideController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("POST".equals(method) && "/api/guides".equals(path)) {
            handleCreate(ex);
        } else if ("POST".equals(method) && "/api/guides/deliver".equals(path)) {
            handleDeliver(ex);
        } else if ("GET".equals(method) && "/api/guides".equals(path)) {
            handleListForPatient(ex);
        } else if ("GET".equals(method) && parts.length == 4) {
            handleGet(ex, parts[3]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        GuideCreateRequestDTO req = new GuideCreateRequestDTO();
        req.setAgencyId(body.get("agencyId"));
        req.setSurgeryType(body.get("surgeryType"));
        req.setTitleEn(body.get("titleEn"));
        req.setContentEn(body.get("contentEn"));
        req.setPdfUrl(body.get("pdfUrl"));
        sendJson(ex, 201, guideToMap(guideController.createGuide(req)));
    }

    private void handleDeliver(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        GuideDeliveryRequestDTO req = new GuideDeliveryRequestDTO();
        req.setRecoveryGuideId(body.get("recoveryGuideId"));
        req.setPatientId(body.get("patientId"));
        req.setDeliveredBy(body.get("deliveredBy"));
        guideController.deliverGuide(req);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("result", "delivered");
        sendJson(ex, 200, m);
    }

    private void handleGet(HttpExchange ex, String id) throws IOException {
        sendJson(ex, 200, guideToMap(guideController.getGuide(id)));
    }

    private void handleListForPatient(HttpExchange ex) throws IOException {
        String patientId = queryParam(ex, "patientId");
        if (patientId == null) { sendError(ex, 400, "Query parameter 'patientId' is required."); return; }
        List<RecoveryGuideDTO> list = guideController.getGuidesForPatient(patientId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (RecoveryGuideDTO g : list) items.add(guideToMap(g));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private Map<String, Object> guideToMap(RecoveryGuideDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("recoveryGuideId", dto.getRecoveryGuideId());
        m.put("agencyId",        dto.getAgencyId());
        m.put("surgeryType",     dto.getSurgeryType());
        m.put("titleEn",         dto.getTitleEn());
        m.put("contentEn",       dto.getContentEn());
        m.put("pdfUrl",          dto.getPdfUrl());
        m.put("createdAt",       dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null);
        return m;
    }
}
