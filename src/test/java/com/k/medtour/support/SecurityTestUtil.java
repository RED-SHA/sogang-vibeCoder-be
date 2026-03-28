package com.k.medtour.support;

import com.k.medtour.global.auth.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Controller 테스트에서 커스텀 UserPrincipal을 SecurityContext에 설정하기 위한 유틸리티.
 * {@code @WithMockUser}는 Spring Security의 기본 {@code User} 객체만 생성하므로,
 * {@code @AuthenticationPrincipal UserPrincipal}이 null이 되는 문제를 해결합니다.
 */
public class SecurityTestUtil {

    /**
     * MockMvc RequestPostProcessor를 반환합니다.
     * 사용법: mockMvc.perform(get("/api/...").with(mockPrincipal(1L, "ADMIN")))
     */
    public static RequestPostProcessor mockPrincipal(Long memberId, String role) {
        UserPrincipal principal = new UserPrincipal(memberId, role);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        return authentication(auth);
    }

    /**
     * SecurityContext에 직접 인증 정보를 설정합니다.
     * {@code @BeforeEach}에서 사용할 수 있습니다.
     */
    public static void setAuthentication(Long memberId, String role) {
        UserPrincipal principal = new UserPrincipal(memberId, role);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * SecurityContext를 초기화합니다.
     * {@code @AfterEach}에서 사용할 수 있습니다.
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
