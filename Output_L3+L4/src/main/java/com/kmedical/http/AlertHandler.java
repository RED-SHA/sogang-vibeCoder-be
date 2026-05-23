package com.kmedical.http;

import com.kmedical.control.AlertController;
import com.kmedical.domain.enums.AlertChannel;
import com.kmedical.domain.enums.AlertType;
import com.kmedical.dto.alert.AlertCreateRequestDTO;
import com.kmedical.dto.alert.AlertDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * SRV-C13 AlertController HTTP 매핑
 *
 * POST /api/alerts           알림 생성·발송
 * GET  /api/alerts?userId=   사용자 알림 이력 조회
 */
public class AlertHandler extends BaseHandler {

    private final AlertController alertController;

    public AlertHandler(AlertController alertController) {
        this.alertController = alertController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        if ("POST".equals(method) && "/api/alerts".equals(path)) {
            handleSend(ex);
        } else if ("GET".equals(method) && "/api/alerts".equals(path)) {
            handleList(ex);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleSend(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        AlertCreateRequestDTO req = new AlertCreateRequestDTO(
                body.get("alertType") != null ? AlertType.valueOf(body.get("alertType")) : null,
                body.get("channel") != null ? AlertChannel.valueOf(body.get("channel")) : null,
                body.get("recipientUserId"),
                body.get("content")
        );
        sendJson(ex, 201, alertToMap(alertController.sendAlert(req)));
    }

    private void handleList(HttpExchange ex) throws IOException {
        String userId = queryParam(ex, "userId");
        if (userId == null) { sendError(ex, 400, "Query parameter 'userId' is required."); return; }
        List<AlertDTO> list = alertController.getAlertsForUser(userId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (AlertDTO a : list) items.add(alertToMap(a));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private Map<String, Object> alertToMap(AlertDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("alertId",         dto.getAlertId());
        m.put("alertType",       dto.getAlertType() != null ? dto.getAlertType().name() : null);
        m.put("channel",         dto.getChannel() != null ? dto.getChannel().name() : null);
        m.put("recipientUserId", dto.getRecipientUserId());
        m.put("content",         dto.getContent());
        m.put("status",          dto.getStatus() != null ? dto.getStatus().name() : null);
        m.put("sentAt",          dto.getSentAt() != null ? dto.getSentAt().toString() : null);
        m.put("failureReason",   dto.getFailureReason());
        return m;
    }
}
