package com.k.medtour.domain.admin.dto;

public record OAuthLoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserInfo user
) {
    public static OAuthLoginResponse of(String accessToken, long expiresIn, UserInfo user) {
        return new OAuthLoginResponse(accessToken, "Bearer", expiresIn, user);
    }
}
