package com.k.medtour.domain.journey.dto;

import com.k.medtour.domain.journey.enums.ScheduleItemStatus;

import java.time.LocalDateTime;

public record StatusUpdateResponse(
        Long scheduleItemId,
        ScheduleItemStatus previousStatus,
        ScheduleItemStatus newStatus,
        LocalDateTime updatedAt,
        UpdatedByDto updatedBy
) {
    public record UpdatedByDto(Long staffId, String name) {
    }
}
