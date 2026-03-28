package com.k.medtour.domain.aftercare.dto;

import com.k.medtour.domain.aftercare.entity.StaffReport;

import java.time.LocalDateTime;

public record StaffReportResponse(
        Long id,
        Long journeyId,
        Long staffId,
        String reportContent,
        Double workHours,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {
    public static StaffReportResponse from(StaffReport entity) {
        return new StaffReportResponse(
                entity.getId(),
                entity.getJourneyId(),
                entity.getStaffId(),
                entity.getReportContent(),
                entity.getWorkHours(),
                entity.getCompletedAt(),
                entity.getCreatedAt()
        );
    }
}
