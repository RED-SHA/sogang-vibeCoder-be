package com.k.medtour.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MagicLinkRequest(
        @NotBlank(message = "target은 필수입니다.")
        String target,

        @NotNull(message = "targetType은 필수입니다.")
        String targetType,

        @NotBlank(message = "role은 필수입니다.")
        String role,

        String language
) {
}
