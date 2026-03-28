package com.k.medtour.domain.journey.dto;

import java.time.LocalDateTime;

public record ScheduleItemUpdateRequest(
        LocalDateTime scheduledAt,
        String title,
        String description,
        Integer durationMinutes,
        LocationDto location,
        String changeReason
) {
}
