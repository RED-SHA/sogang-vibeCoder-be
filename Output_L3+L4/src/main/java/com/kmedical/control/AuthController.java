package com.kmedical.control;

import com.kmedical.adapter.OAuthAdapter;
import com.kmedical.domain.entity.Admin;
import com.kmedical.domain.entity.Patient;
import com.kmedical.domain.entity.User;
import com.kmedical.domain.enums.OAuthProviderType;
import com.kmedical.domain.enums.SystemState;
import com.kmedical.dto.auth.OAuthLoginRequestDTO;
import com.kmedical.dto.auth.OAuthLoginResponseDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.MaskingUtil;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C01 — AuthController
 * 책임: OAuth 검증, 세션 발급, Operator StartUp/CloseDown.
 * «state dependent control»
 * UC: UC-X01, UC-P02, UC-S01
 *
 * NFR 적용: AuditLogger(LOGIN/LOGOUT/STARTUP/CLOSEDOWN), MaskingUtil(email/subjectId)
 * Thread-safe: ConcurrentHashMap 사용
 */
public class AuthController {

    private SystemState currentState;
    private final OAuthAdapter oAuthAdapter;
    private final Map<String, User>   userStore    = new ConcurrentHashMap<>();
    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    public AuthController(OAuthAdapter oAuthAdapter) {
        this.oAuthAdapter = oAuthAdapter;
        this.currentState = SystemState.RUNNING;
        SystemStateRegistry.getInstance().setState(SystemState.RUNNING);
    }

    // ── Operator UC ───────────────────────────────────────────────────────────

    public void startUp(String operatorId) {
        currentState = SystemState.RUNNING;
        SystemStateRegistry.getInstance().setState(SystemState.RUNNING);
        AuditLogger.log("SYSTEM_STARTUP", operatorId, "SYSTEM", true, "System transitioned to RUNNING.");
    }

    public void closeDown(String operatorId) {
        currentState = SystemState.CLOSED_DOWN;
        SystemStateRegistry.getInstance().setState(SystemState.CLOSED_DOWN);
        AuditLogger.log("SYSTEM_CLOSEDOWN", operatorId, "SYSTEM", true, "System transitioned to CLOSED_DOWN.");
    }

    public SystemState getCurrentState() { return currentState; }

    // ── Guard ─────────────────────────────────────────────────────────────────

    private void guardNotClosedDown() {
        if (currentState == SystemState.CLOSED_DOWN) {
            AuditLogger.closedDownAccess("AuthController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    // ── UC-X01 / UC-P02 / UC-S01: OAuth 로그인 ───────────────────────────────

    /**
     * OAuth 인가 코드를 검증하고 세션 토큰을 발급한다.
     * NFR-SEC: authorizationCode 로그 노출 금지, email/subjectId 마스킹
     * NFR-LOG: AUTH_LOGIN 감사 로그
     */
    public OAuthLoginResponseDTO loginWithOAuth(OAuthLoginRequestDTO request) {
        guardNotClosedDown();

        // 1차 입력 검증 (NFR-DD)
        ValidationUtil.requireNotNull(request, "OAuthLoginRequestDTO");
        ValidationUtil.requireNotNull(request.getProvider(), "provider");
        ValidationUtil.requireNotBlank(request.getAuthorizationCode(), "authorizationCode");
        ValidationUtil.requireHttpsUrl(request.getRedirectUri(), "redirectUri");

        String userId = null;
        try {
            String subjectId = oAuthAdapter.verifyAndGetSubjectId(
                    request.getProvider(),
                    request.getAuthorizationCode(),
                    request.getRedirectUri()
            );
            String email = oAuthAdapter.fetchEmail(request.getProvider(), subjectId);

            User user = findOrCreateUser(request.getProvider(), subjectId, email);
            user.setLastLoginAt(LocalDateTime.now());
            userId = user.getUserId();

            String sessionToken = UUID.randomUUID().toString();
            sessionStore.put(sessionToken, userId);

            boolean isNewUser = user.getCreatedAt() != null &&
                    user.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(5));

            AuditLogger.log("AUTH_LOGIN",
                    userId,
                    MaskingUtil.maskEmail(email),
                    true,
                    "provider=" + request.getProvider().name() + " subjectId=" + MaskingUtil.maskSubjectId(subjectId));

            return new OAuthLoginResponseDTO(sessionToken, userId, resolveUserType(user), isNewUser);

        } catch (Exception e) {
            AuditLogger.log("AUTH_LOGIN", userId, "UNKNOWN", false, e.getMessage());
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) throw e;
            throw new IllegalStateException("OAuth login failed: " + e.getMessage());
        }
    }

    /**
     * 세션 토큰으로 사용자 ID를 조회한다.
     */
    public String resolveSession(String sessionToken) {
        guardNotClosedDown();
        if (sessionToken == null || !sessionStore.containsKey(sessionToken)) {
            throw new IllegalArgumentException("Invalid or expired session token.");
        }
        return sessionStore.get(sessionToken);
    }

    /**
     * 세션을 무효화(로그아웃)한다.
     */
    public void invalidateSession(String sessionToken) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(sessionToken, "sessionToken");
        sessionStore.remove(sessionToken);
        AuditLogger.log("AUTH_LOGOUT", null, MaskingUtil.maskToken(sessionToken), true, "Session invalidated.");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private User findOrCreateUser(OAuthProviderType provider, String subjectId, String email) {
        for (User u : userStore.values()) {
            if (provider.equals(u.getOauthProvider()) && subjectId.equals(u.getOauthSubjectId())) {
                return u;
            }
        }
        Patient newUser = new Patient();
        newUser.setUserId(UUID.randomUUID().toString());
        newUser.setEmail(email);
        newUser.setOauthProvider(provider);
        newUser.setOauthSubjectId(subjectId);
        newUser.setCreatedAt(LocalDateTime.now());
        userStore.put(newUser.getUserId(), newUser);
        return newUser;
    }

    private String resolveUserType(User user) {
        if (user instanceof Admin)   return "ADMIN";
        if (user instanceof Patient) return "PATIENT";
        return "STAFF";
    }
}
