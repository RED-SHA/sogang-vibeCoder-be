package com.k.medtour.domain.journey.dto;

import com.k.medtour.domain.journey.entity.JourneyScheduleItem;
import com.k.medtour.domain.journey.enums.ScheduleItemStatus;
import com.k.medtour.domain.journey.enums.ScheduleItemType;

import java.time.LocalDateTime;

public record ScheduleItemResponse(
        Long id,
        Long journeyId,
        LocalDateTime scheduledAt,
        String title,
        ScheduleItemType type,
        ScheduleItemStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ScheduleItemResponse from(JourneyScheduleItem entity) {
        return new ScheduleItemResponse(
                entity.getId(),
                entity.getJourney().getId(),
                entity.getScheduledAt(),
                entity.getTitle(),
                entity.getType(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
