package com.k.medtour.domain.admin.service;

import com.k.medtour.domain.admin.dto.*;
import com.k.medtour.domain.admin.entity.*;
import com.k.medtour.domain.admin.enums.MagicLinkTargetType;
import com.k.medtour.domain.admin.repository.*;
import com.k.medtour.global.auth.jwt.JwtProperties;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private MagicLinkRepository magicLinkRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private MemberConsentRepository memberConsentRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private JwtProperties jwtProperties;

    @Nested
    @DisplayName("OAuth 로그인")
    class OAuthLoginTest {

        @Test
        @DisplayName("성공 - 새 사용자가 Google OAuth로 로그인하면 회원이 생성된다")
        void oauthLogin_success_newUser() {
            // Given
            String provider = "google";
            OAuthLoginRequest request = new OAuthLoginRequest("test-id-token",
                    new OAuthLoginRequest.DeviceInfo("WEB", "en"));

            Role patientRole = Role.builder().name("ROLE_PATIENT").description("환자").build();
            given(memberRepository.findByOauthProviderAndOauthId(eq(provider), anyString())).willReturn(Optional.empty());
            given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());
            given(roleRepository.findByName("ROLE_PATIENT")).willReturn(Optional.of(patientRole));
            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(jwtTokenProvider.createAccessToken(any(), anyString(), anyList())).willReturn("access-token");
            given(jwtTokenProvider.createRefreshToken(any())).willReturn("refresh-token");
            given(jwtProperties.accessTokenExpiration()).willReturn(1800000L);
            given(jwtProperties.refreshTokenExpiration()).willReturn(604800000L);

            // When
            OAuthLoginResponse response = authService.oauthLogin(provider, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
            assertThat(response.user().isNewUser()).isTrue();
        }

        @Test
        @DisplayName("실패 - 지원하지 않는 OAuth 제공자이면 예외가 발생한다")
        void oauthLogin_fail_unsupportedProvider() {
            // Given
            OAuthLoginRequest request = new OAuthLoginRequest("test-token",
                    new OAuthLoginRequest.DeviceInfo("WEB", "en"));

            // When & Then
            assertThatThrownBy(() -> authService.oauthLogin("facebook", request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
    }

    @Nested
    @DisplayName("매직 링크 발급")
    class MagicLinkCreateTest {

        @Test
        @DisplayName("성공 - 유효한 이메일로 매직 링크가 발급된다")
        void createMagicLink_success() {
            // Given
            MagicLinkRequest request = new MagicLinkRequest("patient@example.com", "EMAIL", "ROLE_PATIENT", "en");
            given(magicLinkRepository.countRecentByTargetEmail(anyString(), any(LocalDateTime.class))).willReturn(0L);
            given(magicLinkRepository.save(any(MagicLink.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            MagicLinkResponse response = authService.createMagicLink(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.expiresIn()).isEqualTo(600L);
            assertThat(response.targetMasked()).contains("***");
        }

        @Test
        @DisplayName("실패 - 분당 3회 초과하면 발급이 거부된다")
        void createMagicLink_fail_rateLimit() {
            // Given
            MagicLinkRequest request = new MagicLinkRequest("patient@example.com", "EMAIL", "ROLE_PATIENT", "en");
            given(magicLinkRepository.countRecentByTargetEmail(anyString(), any(LocalDateTime.class))).willReturn(3L);

            // When & Then
            assertThatThrownBy(() -> authService.createMagicLink(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAGIC_LINK_RATE_LIMIT);
        }
    }

    @Nested
    @DisplayName("매직 링크 인증")
    class MagicLinkVerifyTest {

        @Test
        @DisplayName("실패 - 만료된 매직 링크이면 예외가 발생한다")
        void verifyMagicLink_fail_expired() {
            // Given
            UUID token = UUID.randomUUID();
            MagicLinkVerifyRequest request = new MagicLinkVerifyRequest(token.toString(), java.time.LocalDate.of(1990, 5, 15));

            MagicLink magicLink = MagicLink.builder()
                    .token(token)
                    .targetEmail("test@example.com")
                    .targetType(MagicLinkTargetType.EMAIL)
                    .role("ROLE_PATIENT")
                    .expiresAt(LocalDateTime.now().minusMinutes(1))
                    .build();

            given(magicLinkRepository.findByToken(token)).willReturn(Optional.of(magicLink));

            // When & Then
            assertThatThrownBy(() -> authService.verifyMagicLink(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAGIC_LINK_EXPIRED);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 토큰이면 예외가 발생한다")
        void verifyMagicLink_fail_notFound() {
            // Given
            UUID token = UUID.randomUUID();
            MagicLinkVerifyRequest request = new MagicLinkVerifyRequest(token.toString(), java.time.LocalDate.of(1990, 5, 15));
            given(magicLinkRepository.findByToken(token)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.verifyMagicLink(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAGIC_LINK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class RefreshTokenTest {

        @Test
        @DisplayName("실패 - 유효하지 않은 Refresh Token이면 예외가 발생한다")
        void refreshToken_fail_invalid() {
            // Given
            given(refreshTokenRepository.findByToken("invalid-token")).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("실패 - 빈 Refresh Token이면 예외가 발생한다")
        void refreshToken_fail_empty() {
            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(""))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class LogoutTest {

        @Test
        @DisplayName("성공 - 로그아웃 시 Refresh Token이 무효화된다")
        void logout_success() {
            // Given
            Long memberId = 1L;

            // When
            authService.logout(memberId);

            // Then
            verify(refreshTokenRepository).revokeAllByMemberId(memberId);
        }
    }

    @Nested
    @DisplayName("약관 동의")
    class ConsentTest {

        @Test
        @DisplayName("실패 - 필수 동의 항목이 미체크이면 예외가 발생한다")
        void submitConsent_fail_requiredFields() {
            // Given
            ConsentRequest request = new ConsentRequest(false, true, true, false, "2026-03-01");

            // When & Then
            assertThatThrownBy(() -> authService.submitConsent(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CONSENT_REQUIRED_FIELDS);
        }

        @Test
        @DisplayName("성공 - 약관 동의 내역이 조회된다")
        void getConsent_success() {
            // Given
            Long memberId = 1L;
            Member member = Member.builder().email("test@test.com").name("Test").role(Role.builder().name("ROLE_PATIENT").build()).build();
            MemberConsent consent = MemberConsent.builder()
                    .member(member)
                    .termsOfService(true)
                    .privacyPolicy(true)
                    .medicalDataConsent(true)
                    .marketingConsent(false)
                    .consentVersion("2026-03-01")
                    .consentedAt(LocalDateTime.now())
                    .build();

            given(memberConsentRepository.findTopByMemberIdOrderByConsentedAtDesc(memberId))
                    .willReturn(Optional.of(consent));

            // When
            ConsentResponse response = authService.getConsent(memberId);

            // Then
            assertThat(response.termsOfService()).isTrue();
            assertThat(response.privacyPolicy()).isTrue();
            assertThat(response.consentVersion()).isEqualTo("2026-03-01");
        }
    }

    @Nested
    @DisplayName("역할 변경")
    class RoleChangeTest {

        @Test
        @DisplayName("실패 - 자기 자신의 역할을 변경하면 예외가 발생한다")
        void changeUserRole_fail_self() {
            // Given
            Long adminId = 1L;
            RoleChangeRequest request = new RoleChangeRequest(2L);

            // When & Then
            assertThatThrownBy(() -> authService.changeUserRole(adminId, adminId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CANNOT_CHANGE_OWN_ROLE);
        }

        @Test
        @DisplayName("실패 - 대상 사용자가 존재하지 않으면 예외가 발생한다")
        void changeUserRole_fail_memberNotFound() {
            // Given
            Long adminId = 1L;
            Long targetUserId = 99L;
            RoleChangeRequest request = new RoleChangeRequest(2L);
            given(memberRepository.findByIdWithRole(targetUserId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.changeUserRole(adminId, targetUserId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }
    }
}
