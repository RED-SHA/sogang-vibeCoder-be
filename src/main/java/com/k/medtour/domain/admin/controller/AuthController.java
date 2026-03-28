package com.k.medtour.domain.admin.controller;

import com.k.medtour.domain.admin.dto.*;
import com.k.medtour.domain.admin.service.AuthService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * OAuth2 소셜 로그인
     * POST /api/v1/auth/oauth/{provider}
     */
    @PostMapping("/oauth/{provider}")
    public ApiResponse<OAuthLoginResponse> oauthLogin(
            @PathVariable String provider,
            @Valid @RequestBody OAuthLoginRequest request,
            HttpServletResponse response) {

        OAuthLoginResponse loginResponse = authService.oauthLogin(provider, request);

        // Refresh Token을 HttpOnly Cookie로 설정
        String refreshToken = authService.getRefreshTokenForMember(loginResponse.user().id());
        addRefreshTokenCookie(response, refreshToken);

        return ApiResponse.success("로그인 성공", loginResponse);
    }

    /**
     * 매직 링크 발급
     * POST /api/v1/auth/magic-link
     */
    @PostMapping("/magic-link")
    public ApiResponse<MagicLinkResponse> createMagicLink(@Valid @RequestBody MagicLinkRequest request) {
        MagicLinkResponse magicLinkResponse = authService.createMagicLink(request);
        return ApiResponse.success("매직 링크가 전송되었습니다", magicLinkResponse);
    }

    /**
     * 매직 링크 인증 + 2FA
     * POST /api/v1/auth/magic-link/verify
     */
    @PostMapping("/magic-link/verify")
    public ApiResponse<MagicLinkVerifyResponse> verifyMagicLink(@Valid @RequestBody MagicLinkVerifyRequest request) {
        MagicLinkVerifyResponse verifyResponse = authService.verifyMagicLink(request);
        return ApiResponse.success("인증 성공", verifyResponse);
    }

    /**
     * 토큰 갱신
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        return ApiResponse.success("토큰 갱신 성공", tokenResponse);
    }

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletResponse response) {

        authService.logout(principal.memberId());

        // Refresh Token Cookie 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ApiResponse.success("로그아웃 성공", null);
    }

    /**
     * 약관 동의
     * POST /api/v1/auth/consent
     */
    @PostMapping("/consent")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<ConsentResponse> submitConsent(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ConsentRequest request) {

        ConsentResponse consentResponse = authService.submitConsent(principal.memberId(), request);
        return ApiResponse.success("동의 처리 완료", consentResponse);
    }

    /**
     * 약관 동의 내역 조회
     * GET /api/v1/auth/consent
     */
    @GetMapping("/consent")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<ConsentResponse> getConsent(@AuthenticationPrincipal UserPrincipal principal) {
        ConsentResponse consentResponse = authService.getConsent(principal.memberId());
        return ApiResponse.success("조회 성공", consentResponse);
    }

    /**
     * 역할 목록 조회
     * GET /api/v1/auth/roles
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<RoleResponse>> getRoles() {
        List<RoleResponse> roles = authService.getRoles();
        return ApiResponse.success("조회 성공", roles);
    }

    /**
     * 사용자 역할 변경
     * PUT /api/v1/auth/users/{userId}/role
     */
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleChangeResponse> changeUserRole(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userId,
            @Valid @RequestBody RoleChangeRequest request) {

        RoleChangeResponse roleChangeResponse = authService.changeUserRole(principal.memberId(), userId, request);
        return ApiResponse.success("역할 변경 완료", roleChangeResponse);
    }

    // ===== Private helpers =====

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
    }
}
