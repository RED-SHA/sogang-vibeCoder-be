package com.kmedical.http;

import com.kmedical.control.AgencyController;
import com.kmedical.dto.agency.AgencyDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SRV-C05 AgencyController HTTP 매핑
 *
 * GET  /api/agencies/{id}         에이전시 조회
 * POST /api/agencies              에이전시 생성/수정
 * PUT  /api/agencies/{id}/verify  라이선스 인증 상태 변경
 */
public class AgencyHandler extends BaseHandler {

    private final AgencyController agencyController;

    public AgencyHandler(AgencyController agencyController) {
        this.agencyController = agencyController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("GET".equals(method) && parts.length == 4) {
            handleGet(ex, parts[3]);
        } else if ("POST".equals(method) && "/api/agencies".equals(path)) {
            handleSave(ex);
        } else if ("PUT".equals(method) && parts.length == 5 && "verify".equals(parts[4])) {
            handleVerify(ex, parts[3]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleGet(HttpExchange ex, String id) throws IOException {
        sendJson(ex, 200, agencyToMap(agencyController.getAgency(id)));
    }

    private void handleSave(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        AgencyDTO dto = new AgencyDTO();
        dto.setAgencyId(body.get("agencyId"));
        dto.setNameKo(body.get("nameKo"));
        dto.setNameEn(body.get("nameEn"));
        dto.setLicenseNumber(body.get("licenseNumber"));
        dto.setLicenseDocumentUrl(body.get("licenseDocumentUrl"));
        dto.setContactEmail(body.get("contactEmail"));
        if (body.get("isVerified") != null) dto.setIsVerified(Boolean.parseBoolean(body.get("isVerified")));
        sendJson(ex, 200, agencyToMap(agencyController.saveAgency(dto)));
    }

    private void handleVerify(HttpExchange ex, String id) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        boolean verified = Boolean.parseBoolean(body.getOrDefault("verified", "true"));
        sendJson(ex, 200, agencyToMap(agencyController.verifyAgency(id, verified)));
    }

    private Map<String, Object> agencyToMap(AgencyDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("agencyId",           dto.getAgencyId());
        m.put("nameKo",             dto.getNameKo());
        m.put("nameEn",             dto.getNameEn());
        m.put("licenseNumber",      dto.getLicenseNumber());
        m.put("licenseDocumentUrl", dto.getLicenseDocumentUrl());
        m.put("isVerified",         dto.getIsVerified());
        m.put("contactEmail",       dto.getContactEmail());
        m.put("updatedAt",          dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : null);
        return m;
    }
}
