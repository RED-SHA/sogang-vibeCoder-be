package com.k.medtour.domain.journey.dto;

import com.k.medtour.domain.journey.entity.JourneyTemplate;
import com.k.medtour.domain.journey.enums.TemplateCategory;

import java.time.LocalDateTime;

public record TemplateListResponse(
        Long id,
        String name,
        TemplateCategory category,
        Integer durationDays,
        Integer itemCount,
        Integer usageCount,
        LocalDateTime createdAt
) {
    public static TemplateListResponse from(JourneyTemplate entity) {
        return new TemplateListResponse(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getDurationDays(),
                entity.getItems().size(),
                entity.getUsageCount(),
                entity.getCreatedAt()
        );
    }
}
