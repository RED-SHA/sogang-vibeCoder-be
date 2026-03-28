package com.k.medtour.domain.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k.medtour.domain.admin.dto.*;
import com.k.medtour.domain.admin.service.AuthService;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import com.k.medtour.global.exception.GlobalExceptionHandler;
import com.k.medtour.support.SecurityTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void tearDown() {
        SecurityTestUtil.clearAuthentication();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/oauth/{provider}")
    class OAuthLoginApiTest {

        @Test
        @DisplayName("성공 - OAuth 로그인 요청이 정상 처리된다")
        void oauthLogin_success() throws Exception {
            // Given
            OAuthLoginRequest request = new OAuthLoginRequest("test-id-token",
                    new OAuthLoginRequest.DeviceInfo("WEB", "en"));
            UserInfo userInfo = new UserInfo(1L, "test@test.com", "Test User", "ROLE_PATIENT", true);
            OAuthLoginResponse response = OAuthLoginResponse.of("access-token", 1800, userInfo);

            given(authService.oauthLogin(eq("google"), any(OAuthLoginRequest.class))).willReturn(response);
            given(authService.getRefreshTokenForMember(1L)).willReturn("refresh-token");

            // When & Then
            mockMvc.perform(post("/api/v1/auth/oauth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.data.user.email").value("test@test.com"));
        }

        @Test
        @DisplayName("실패 - 지원하지 않는 OAuth 제공자이면 400 반환")
        void oauthLogin_fail_unsupportedProvider() throws Exception {
            // Given
            OAuthLoginRequest request = new OAuthLoginRequest("test-id-token",
                    new OAuthLoginRequest.DeviceInfo("WEB", "en"));

            given(authService.oauthLogin(eq("facebook"), any(OAuthLoginRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/oauth/facebook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/magic-link")
    class MagicLinkApiTest {

        @Test
        @DisplayName("성공 - 매직 링크 발급 요청이 정상 처리된다")
        void createMagicLink_success() throws Exception {
            // Given
            MagicLinkRequest request = new MagicLinkRequest("patient@example.com", "EMAIL", "ROLE_PATIENT", "en");
            MagicLinkResponse response = new MagicLinkResponse(600L, "pat***@example.com");

            given(authService.createMagicLink(any(MagicLinkRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/magic-link")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.expiresIn").value(600))
                    .andExpect(jsonPath("$.data.targetMasked").value("pat***@example.com"));
        }

        @Test
        @DisplayName("실패 - 분당 3회 초과하면 429 반환")
        void createMagicLink_fail_rateLimit() throws Exception {
            // Given
            MagicLinkRequest request = new MagicLinkRequest("patient@example.com", "EMAIL", "ROLE_PATIENT", "en");

            given(authService.createMagicLink(any(MagicLinkRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.MAGIC_LINK_RATE_LIMIT));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/magic-link")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isTooManyRequests());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class RefreshTokenApiTest {

        @Test
        @DisplayName("성공 - 토큰 갱신이 정상 처리된다")
        void refresh_success() throws Exception {
            // Given
            TokenResponse response = TokenResponse.of("new-access-token", 1800);
            given(authService.refreshToken("valid-refresh-token")).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new jakarta.servlet.http.Cookie("refreshToken", "valid-refresh-token")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/roles")
    class RolesApiTest {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "ADMIN");
        }

        @Test
        @DisplayName("성공 - 역할 목록이 조회된다")
        void getRoles_success() throws Exception {
            // Given
            List<RoleResponse> roles = List.of(
                    new RoleResponse(1L, "ROLE_ADMIN", "관리자", List.of("DASHBOARD_VIEW")),
                    new RoleResponse(2L, "ROLE_PATIENT", "환자", List.of("JOURNEY_VIEW"))
            );
            given(authService.getRoles()).willReturn(roles);

            // When & Then
            mockMvc.perform(get("/api/v1/auth/roles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].name").value("ROLE_ADMIN"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/auth/users/{userId}/role")
    class ChangeRoleApiTest {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "ADMIN");
        }

        @Test
        @DisplayName("실패 - 자기 자신의 역할 변경 시 403 반환")
        void changeRole_fail_self() throws Exception {
            // Given
            RoleChangeRequest request = new RoleChangeRequest(2L);
            given(authService.changeUserRole(any(), eq(1L), any(RoleChangeRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.CANNOT_CHANGE_OWN_ROLE));

            // When & Then
            mockMvc.perform(put("/api/v1/auth/users/1/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }
}
