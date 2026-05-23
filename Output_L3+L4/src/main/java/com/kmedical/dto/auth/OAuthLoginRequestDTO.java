package com.kmedical.dto.auth;

import com.kmedical.domain.enums.OAuthProviderType;

/** Interface → AuthController 간 OAuth 로그인 요청 전달 DTO */
public class OAuthLoginRequestDTO {

    private OAuthProviderType provider;
    private String authorizationCode;
    private String redirectUri;

    public OAuthLoginRequestDTO() {}

    public OAuthLoginRequestDTO(OAuthProviderType provider, String authorizationCode, String redirectUri) {
        this.provider = provider;
        this.authorizationCode = authorizationCode;
        this.redirectUri = redirectUri;
    }

    public OAuthProviderType getProvider() { return provider; }
    public void setProvider(OAuthProviderType provider) { this.provider = provider; }

    public String getAuthorizationCode() { return authorizationCode; }
    public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
}
