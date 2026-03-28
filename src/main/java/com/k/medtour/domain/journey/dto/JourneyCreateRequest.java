package com.k.medtour.domain.journey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record JourneyCreateRequest(
        @NotNull Long patientId,
        @NotNull Long templateId,
        @NotNull LocalDate startDate,
        @NotBlank String title,
        String notes
) {
}
