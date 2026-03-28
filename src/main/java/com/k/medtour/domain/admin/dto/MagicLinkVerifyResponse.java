package com.k.medtour.domain.admin.dto;

public record MagicLinkVerifyResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserInfo user
) {
    public static MagicLinkVerifyResponse of(String accessToken, long expiresIn, UserInfo user) {
        return new MagicLinkVerifyResponse(accessToken, "Bearer", expiresIn, user);
    }
}
