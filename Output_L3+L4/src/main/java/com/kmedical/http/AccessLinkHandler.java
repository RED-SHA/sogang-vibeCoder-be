package com.kmedical.http;

import com.kmedical.control.AccessLinkController;
import com.kmedical.domain.enums.AccessLinkType;
import com.kmedical.dto.accesslink.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SRV-C02 AccessLinkController HTTP 매핑
 *
 * POST   /api/access-links          접속 링크 생성
 * POST   /api/access-links/verify   링크 검증 (매직링크 + 생년월일)
 * DELETE /api/access-links/{token}  링크 무효화
 */
public class AccessLinkHandler extends BaseHandler {

    private final AccessLinkController accessLinkController;

    public AccessLinkHandler(AccessLinkController accessLinkController) {
        this.accessLinkController = accessLinkController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("POST".equals(method) && "/api/access-links".equals(path)) {
            handleCreate(ex);
        } else if ("POST".equals(method) && "/api/access-links/verify".equals(path)) {
            handleVerify(ex);
        } else if ("DELETE".equals(method) && parts.length == 4) {
            handleInvalidate(ex, parts[3]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleCreate(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        AccessLinkCreateRequestDTO req = new AccessLinkCreateRequestDTO();
        if (body.get("linkType") != null) req.setLinkType(AccessLinkType.valueOf(body.get("linkType")));
        req.setTargetId(body.get("targetId"));
        req.setRecipientUserId(body.get("recipientUserId"));

        AccessLinkDTO result = accessLinkController.createAccessLink(req);
        sendJson(ex, 201, linkToMap(result));
    }

    private void handleVerify(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        AccessLinkVerifyRequestDTO req = new AccessLinkVerifyRequestDTO();
        req.setToken(body.get("token"));
        if (body.get("dateOfBirth") != null) req.setDateOfBirth(LocalDate.parse(body.get("dateOfBirth")));

        AccessLinkVerifyResponseDTO result = accessLinkController.verifyAccessLink(req);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("valid",         result.isValid());
        m.put("linkType",      result.getLinkType() != null ? result.getLinkType().name() : null);
        m.put("targetId",      result.getTargetId());
        m.put("sessionToken",  result.getSessionToken());
        m.put("failureReason", result.getFailureReason());
        sendJson(ex, 200, m);
    }

    private void handleInvalidate(HttpExchange ex, String token) throws IOException {
        accessLinkController.invalidateLink(token);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("result", "invalidated");
        sendJson(ex, 200, m);
    }

    private Map<String, Object> linkToMap(AccessLinkDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("accessLinkId",    dto.getAccessLinkId());
        m.put("token",           dto.getToken());
        m.put("linkType",        dto.getLinkType() != null ? dto.getLinkType().name() : null);
        m.put("targetId",        dto.getTargetId());
        m.put("recipientUserId", dto.getRecipientUserId());
        m.put("expiresAt",       dto.getExpiresAt() != null ? dto.getExpiresAt().toString() : null);
        m.put("invalidated",     dto.isInvalidated());
        return m;
    }
}
