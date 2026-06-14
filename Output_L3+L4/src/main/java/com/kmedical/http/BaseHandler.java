package com.kmedical.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseHandler implements HttpHandler {

    @Override
    public final void handle(HttpExchange ex) throws IOException {
        addCorsHeaders(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        try {
            dispatch(ex);
        } catch (IllegalArgumentException e) {
            sendError(ex, 400, e.getMessage());
        } catch (IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            // ClosedDown 가드 차단 → 503 Service Unavailable
            int status = msg.toLowerCase().contains("closed down") ? 503 : 409;
            sendError(ex, status, msg);
        } catch (Exception e) {
            sendError(ex, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    protected abstract void dispatch(HttpExchange ex) throws IOException;

    protected String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected void sendJson(HttpExchange ex, int status, Map<String, Object> body) throws IOException {
        byte[] bytes = JsonUtil.toJson(body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendError(HttpExchange ex, int status, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        sendJson(ex, status, body);
    }

    protected String queryParam(HttpExchange ex, String name) {
        String query = ex.getRequestURI().getQuery();
        if (query == null) return null;
        for (String kv : query.split("&")) {
            String[] parts = kv.split("=", 2);
            if (parts.length == 2 && name.equals(parts[0])) return parts[1];
        }
        return null;
    }

    private void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}
