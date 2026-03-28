package com.k.medtour.domain.journey.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StaffTodayResponse(
        LocalDate date,
        Integer totalTasks,
        Integer completedTasks,
        List<TaskDto> tasks
) {
    public record TaskDto(
            Long assignmentId,
            Long scheduleItemId,
            Long journeyId,
            LocalDateTime scheduledAt,
            String title,
            String type,
            String status,
            PatientDto patient,
            LocationDto location,
            Integer durationMinutes,
            LocalDateTime completedAt
    ) {
    }

    public record PatientDto(
            Long id,
            String name,
            String nationality
    ) {
    }
}
