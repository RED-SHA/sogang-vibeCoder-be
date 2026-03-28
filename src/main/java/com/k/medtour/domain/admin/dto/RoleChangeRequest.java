package com.k.medtour.domain.admin.dto;

import jakarta.validation.constraints.NotNull;

public record RoleChangeRequest(
        @NotNull(message = "roleId는 필수입니다.")
        Long roleId
) {
}
