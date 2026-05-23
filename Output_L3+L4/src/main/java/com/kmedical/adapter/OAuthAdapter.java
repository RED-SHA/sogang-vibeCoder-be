package com.kmedical.adapter;

import com.kmedical.domain.enums.OAuthProviderType;

/**
 * INF-A01 — OAuthAdapter
 * E05 OAuthService(Google/Apple/Kakao) 연동 인터페이스.
 * SRV-C01 AuthController가 호출한다.
 */
public interface OAuthAdapter {

    /**
     * OAuth 인가 코드를 검증하고 사용자 식별값(subject)을 반환한다.
     *
     * @param provider         소셜 로그인 제공자
     * @param authorizationCode 클라이언트로부터 받은 인가 코드
     * @param redirectUri      클라이언트가 사용한 redirect URI
     * @return OAuth 사용자 식별값 (subject ID)
     * @throws IllegalArgumentException 코드 검증 실패 시
     */
    String verifyAndGetSubjectId(OAuthProviderType provider, String authorizationCode, String redirectUri);

    /**
     * OAuth subject ID로 사용자 이메일을 조회한다.
     *
     * @param provider  소셜 로그인 제공자
     * @param subjectId OAuth 사용자 식별값
     * @return 이메일 주소
     */
    String fetchEmail(OAuthProviderType provider, String subjectId);
}
