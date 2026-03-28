package com.k.medtour.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MagicLinkVerifyRequest(
        @NotBlank(message = "token은 필수입니다.")
        String token,

        @NotNull(message = "birthDate는 필수입니다.")
        LocalDate birthDate
) {
}
