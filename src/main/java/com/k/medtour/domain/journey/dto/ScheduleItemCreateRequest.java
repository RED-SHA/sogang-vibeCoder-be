package com.k.medtour.domain.journey.dto;

import com.k.medtour.domain.journey.enums.ScheduleItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleItemCreateRequest(
        @NotNull LocalDateTime scheduledAt,
        @NotBlank String title,
        @NotNull ScheduleItemType type,
        String description,
        Integer durationMinutes,
        LocationDto location,
        List<String> requiredStaff
) {
}
