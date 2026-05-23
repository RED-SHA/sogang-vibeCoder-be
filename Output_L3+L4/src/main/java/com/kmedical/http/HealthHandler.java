package com.kmedical.http;

import com.kmedical.control.SystemStateRegistry;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** GET /api/health — 시스템 상태 확인 */
public class HealthHandler extends BaseHandler {

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            sendError(ex, 405, "Method Not Allowed");
            return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("systemState", SystemStateRegistry.getInstance().getState().name());
        body.put("timestamp", LocalDateTime.now().toString());
        sendJson(ex, 200, body);
    }
}
