package com.kmedical.control;

import com.kmedical.adapter.OAuthAdapter;
import com.kmedical.domain.entity.Admin;
import com.kmedical.domain.entity.Patient;
import com.kmedical.domain.entity.User;
import com.kmedical.domain.enums.OAuthProviderType;
import com.kmedical.domain.enums.SystemState;
import com.kmedical.dto.auth.OAuthLoginRequestDTO;
import com.kmedical.dto.auth.OAuthLoginResponseDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C01 — AuthController
 * 책임: OAuth 검증, 세션 발급, 매직링크 로그인, Operator StartUp/CloseDown.
 * «state dependent control»: currentState 인스턴스 변수로 시스템 상태 유지.
 * UC: UC-X01, UC-P02, UC-S01
 */
public class AuthController {

    private SystemState currentState;
    private final OAuthAdapter oAuthAdapter;
    private final Map<String, User> userStore;
    private final Map<String, String> sessionStore;

    public AuthController(OAuthAdapter oAuthAdapter) {
        this.oAuthAdapter = oAuthAdapter;
        this.currentState = SystemState.RUNNING;
        this.userStore = new HashMap<>();
        this.sessionStore = new HashMap<>();
        SystemStateRegistry.getInstance().setState(SystemState.RUNNING);
    }

    // ── Operator UC ───────────────────────────────────────────────────────────

    /** Operator StartUp: 시스템을 RUNNING 상태로 전환한다. */
    public void startUp(String operatorId) {
        currentState = SystemState.RUNNING;
        SystemStateRegistry.getInstance().setState(SystemState.RUNNING);
    }

    /** Operator CloseDown: 시스템을 CLOSED_DOWN 상태로 전환한다. */
    public void closeDown(String operatorId) {
        currentState = SystemState.CLOSED_DOWN;
        SystemStateRegistry.getInstance().setState(SystemState.CLOSED_DOWN);
    }

    public SystemState getCurrentState() {
        return currentState;
    }

    // ── Guard ─────────────────────────────────────────────────────────────────

    private void guardNotClosedDown() {
        if (currentState == SystemState.CLOSED_DOWN) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    // ── UC-X01 / UC-P02 / UC-S01: OAuth 로그인 ───────────────────────────────

    /**
     * OAuth 인가 코드를 검증하고 세션 토큰을 발급한다.
     * System Response: OAuth 검증 → User 조회 또는 생성 → 세션 토큰 발급
     */
    public OAuthLoginResponseDTO loginWithOAuth(OAuthLoginRequestDTO request) {
        guardNotClosedDown();

        if (request == null || request.getProvider() == null || request.getAuthorizationCode() == null) {
            throw new IllegalArgumentException("OAuth login request is incomplete.");
        }

        String subjectId = oAuthAdapter.verifyAndGetSubjectId(
                request.getProvider(),
                request.getAuthorizationCode(),
                request.getRedirectUri()
        );
        String email = oAuthAdapter.fetchEmail(request.getProvider(), subjectId);

        User user = findOrCreateUser(request.getProvider(), subjectId, email);
        user.setLastLoginAt(LocalDateTime.now());

        String sessionToken = UUID.randomUUID().toString();
        sessionStore.put(sessionToken, user.getUserId());

        boolean isNewUser = (user.getCreatedAt() != null &&
                user.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(5)));

        String userType = resolveUserType(user);
        return new OAuthLoginResponseDTO(sessionToken, user.getUserId(), userType, isNewUser);
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
        sessionStore.remove(sessionToken);
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
        if (user instanceof Admin) return "ADMIN";
        if (user instanceof Patient) return "PATIENT";
        return "STAFF";
    }
}
