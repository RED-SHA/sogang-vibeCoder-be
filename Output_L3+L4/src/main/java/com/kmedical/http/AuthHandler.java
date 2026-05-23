package com.kmedical.http;

import com.kmedical.control.AuthController;
import com.kmedical.domain.enums.OAuthProviderType;
import com.kmedical.dto.auth.OAuthLoginRequestDTO;
import com.kmedical.dto.auth.OAuthLoginResponseDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SRV-C01 AuthController HTTP 매핑
 *
 * POST /api/auth/login       UC-P02/UC-S01 OAuth 로그인
 * POST /api/auth/logout      세션 무효화
 * POST /api/auth/startup     Operator StartUp
 * POST /api/auth/closedown   Operator CloseDown
 */
public class AuthHandler extends BaseHandler {

    private final AuthController authController;

    public AuthHandler(AuthController authController) {
        this.authController = authController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        if (!"POST".equals(method)) {
            sendError(ex, 405, "Method Not Allowed");
            return;
        }
        switch (path) {
            case "/api/auth/login":    handleLogin(ex);    break;
            case "/api/auth/logout":   handleLogout(ex);   break;
            case "/api/auth/startup":  handleStartUp(ex);  break;
            case "/api/auth/closedown":handleCloseDown(ex);break;
            default: sendError(ex, 404, "Not Found: " + path);
        }
    }

    private void handleLogin(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));

        OAuthLoginRequestDTO req = new OAuthLoginRequestDTO();
        if (body.get("provider") != null) {
            req.setProvider(OAuthProviderType.valueOf(body.get("provider")));
        }
        req.setAuthorizationCode(body.get("authorizationCode"));
        req.setRedirectUri(body.get("redirectUri"));

        OAuthLoginResponseDTO resp = authController.loginWithOAuth(req);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sessionToken", resp.getSessionToken());
        m.put("userId",       resp.getUserId());
        m.put("userType",     resp.getUserType());
        m.put("isNewUser",    resp.isNewUser());
        sendJson(ex, 200, m);
    }

    private void handleLogout(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        authController.invalidateSession(body.get("sessionToken"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("result", "logged out");
        sendJson(ex, 200, m);
    }

    private void handleStartUp(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        authController.startUp(body.getOrDefault("operatorId", "system"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("systemState", "RUNNING");
        sendJson(ex, 200, m);
    }

    private void handleCloseDown(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        authController.closeDown(body.getOrDefault("operatorId", "system"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("systemState", "CLOSED_DOWN");
        sendJson(ex, 200, m);
    }
}
