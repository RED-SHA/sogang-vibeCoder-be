package com.k.medtour.domain.journey.dto;

import com.k.medtour.domain.journey.entity.JourneyTemplateItem;
import com.k.medtour.domain.journey.enums.ScheduleItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TemplateItemDto(
        Long id,
        @NotNull Integer dayOffset,
        @NotBlank String timeOffset,
        @NotBlank String title,
        @NotNull ScheduleItemType type,
        String description,
        Integer durationMinutes,
        LocationDto location,
        List<String> requiredStaff,
        Integer order
) {
    public static TemplateItemDto from(JourneyTemplateItem entity) {
        return new TemplateItemDto(
                entity.getId(),
                entity.getDayOffset(),
                entity.getTimeOffset(),
                entity.getTitle(),
                entity.getType(),
                entity.getDescription(),
                entity.getDurationMinutes(),
                LocationDto.from(entity.getLocation()),
                entity.getRequiredStaff(),
                entity.getSortOrder()
        );
    }
}
