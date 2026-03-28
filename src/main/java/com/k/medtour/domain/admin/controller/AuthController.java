package com.k.medtour.domain.admin.controller;

import com.k.medtour.domain.admin.dto.*;
import com.k.medtour.domain.admin.service.AuthService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "인증", description = "OAuth2 소셜 로그인, 매직 링크, 토큰 갱신, 약관 동의, 역할 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * OAuth2 소셜 로그인
     * POST /api/v1/auth/oauth/{provider}
     */
    @Operation(summary = "OAuth2 소셜 로그인", description = "Google, Apple 등 소셜 프로바이더를 통한 로그인")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/oauth/{provider}")
    public ApiResponse<OAuthLoginResponse> oauthLogin(
            @Parameter(description = "OAuth 프로바이더 (google, apple)", required = true)
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
    @Operation(summary = "매직 링크 발급", description = "비회원 접속용 매직 링크를 생성하여 전송")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "매직 링크 전송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/magic-link")
    public ApiResponse<MagicLinkResponse> createMagicLink(@Valid @RequestBody MagicLinkRequest request) {
        MagicLinkResponse magicLinkResponse = authService.createMagicLink(request);
        return ApiResponse.success("매직 링크가 전송되었습니다", magicLinkResponse);
    }

    /**
     * 매직 링크 인증 + 2FA
     * POST /api/v1/auth/magic-link/verify
     */
    @Operation(summary = "매직 링크 인증", description = "매직 링크 토큰 검증 및 2차 인증(생년월일) 처리")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/magic-link/verify")
    public ApiResponse<MagicLinkVerifyResponse> verifyMagicLink(@Valid @RequestBody MagicLinkVerifyRequest request) {
        MagicLinkVerifyResponse verifyResponse = authService.verifyMagicLink(request);
        return ApiResponse.success("인증 성공", verifyResponse);
    }

    /**
     * 토큰 갱신
     * POST /api/v1/auth/refresh
     */
    @Operation(summary = "토큰 갱신", description = "Refresh Token 쿠키를 이용한 Access Token 재발급")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        return ApiResponse.success("토큰 갱신 성공", tokenResponse);
    }

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     */
    @Operation(summary = "로그아웃", description = "현재 사용자의 세션 종료 및 Refresh Token 삭제")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
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
    @Operation(summary = "약관 동의 제출", description = "환자의 글로벌 규제 동의(T&C, Privacy) 처리")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "동의 처리 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
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
    @Operation(summary = "약관 동의 내역 조회", description = "현재 환자의 약관 동의 상태 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
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
    @Operation(summary = "역할 목록 조회", description = "시스템에 등록된 전체 역할 목록 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
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
    @Operation(summary = "사용자 역할 변경", description = "특정 사용자의 역할을 변경 (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "역할 변경 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
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
