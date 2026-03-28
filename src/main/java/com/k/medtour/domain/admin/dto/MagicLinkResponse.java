package com.k.medtour.domain.admin.dto;

public record MagicLinkResponse(
        long expiresIn,
        String targetMasked
) {
}
