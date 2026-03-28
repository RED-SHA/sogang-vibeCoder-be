package com.k.medtour.domain.aftercare.dto;

import com.k.medtour.domain.aftercare.entity.AftercareGuide;

import java.time.LocalDateTime;
import java.util.Map;

public record AftercareGuideResponse(
        Long id,
        Long journeyId,
        String title,
        String content,
        Map<String, Object> instructions,
        LocalDateTime publishedAt,
        LocalDateTime createdAt
) {
    public static AftercareGuideResponse from(AftercareGuide entity) {
        return new AftercareGuideResponse(
                entity.getId(),
                entity.getJourneyId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getInstructions(),
                entity.getPublishedAt(),
                entity.getCreatedAt()
        );
    }
}
