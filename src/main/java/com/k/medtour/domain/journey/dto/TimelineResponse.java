package com.k.medtour.domain.journey.dto;

import java.time.LocalDate;
import java.util.List;

public record TimelineResponse(
        Long journeyId,
        String title,
        LocalDate currentDate,
        Integer dayNumber,
        Integer totalDays,
        List<TimelineItemDto> items
) {
    public record TimelineItemDto(
            Long id,
            java.time.LocalDateTime scheduledAt,
            String title,
            String type,
            String status,
            String description,
            TimelineLocationDto location,
            java.util.Map<String, TimelineStaffDto> assignedStaff
    ) {
    }

    public record TimelineLocationDto(
            String name,
            String googleMapsUrl
    ) {
    }

    public record TimelineStaffDto(
            String name,
            String phone
    ) {
    }
}
