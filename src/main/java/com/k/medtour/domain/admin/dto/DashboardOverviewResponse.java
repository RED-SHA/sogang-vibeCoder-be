package com.k.medtour.domain.admin.dto;

public record DashboardOverviewResponse(
        long totalPatients,
        long activeJourneys,
        long todaySchedules,
        long unreadChats
) {
}
