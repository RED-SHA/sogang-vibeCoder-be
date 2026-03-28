package com.k.medtour.domain.aftercare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StaffReportCreateRequest(
        @NotNull Long journeyId,
        @NotBlank String reportContent,
        @NotNull Double workHours
) {
}
