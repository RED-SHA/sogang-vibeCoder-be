package com.k.medtour.global.auth;

public record UserPrincipal(
        Long memberId,
        String role
) {
}
