package com.k.medtour.domain.admin.dto;

public record RoleChangeResponse(
        Long userId,
        String previousRole,
        String newRole
) {
}
