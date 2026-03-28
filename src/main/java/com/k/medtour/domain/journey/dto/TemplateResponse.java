package com.k.medtour.domain.journey.dto;

import com.k.medtour.domain.journey.entity.JourneyTemplate;
import com.k.medtour.domain.journey.enums.TemplateCategory;

import java.time.LocalDateTime;
import java.util.List;

public record TemplateResponse(
        Long id,
        String name,
        TemplateCategory category,
        Integer durationDays,
        List<TemplateItemDto> items,
        Integer usageCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TemplateResponse from(JourneyTemplate entity) {
        List<TemplateItemDto> items = entity.getItems().stream()
                .map(TemplateItemDto::from)
                .toList();
        return new TemplateResponse(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getDurationDays(),
                items,
                entity.getUsageCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
