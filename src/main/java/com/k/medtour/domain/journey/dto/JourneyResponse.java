package com.k.medtour.domain.journey.dto;

import com.k.medtour.domain.journey.entity.Journey;
import com.k.medtour.domain.journey.enums.JourneyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record JourneyResponse(
        Long id,
        Long patientId,
        String title,
        JourneyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Integer scheduleItemCount,
        LocalDateTime createdAt
) {
    public static JourneyResponse from(Journey entity) {
        return new JourneyResponse(
                entity.getId(),
                entity.getPatient().getId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getScheduleItems().size(),
                entity.getCreatedAt()
        );
    }
}
