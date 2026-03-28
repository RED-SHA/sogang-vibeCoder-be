package com.k.medtour.domain.aftercare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record AftercareGuideCreateRequest(
        @NotNull Long journeyId,
        @NotBlank String title,
        String content,
        Map<String, Object> instructions
) {
}
