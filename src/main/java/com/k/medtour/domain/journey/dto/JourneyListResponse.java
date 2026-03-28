package com.k.medtour.domain.journey.dto;

import com.k.medtour.domain.journey.entity.Journey;
import com.k.medtour.domain.journey.entity.JourneyScheduleItem;
import com.k.medtour.domain.journey.enums.JourneyStatus;
import com.k.medtour.domain.journey.enums.ScheduleItemStatus;

import java.time.LocalDate;

public record JourneyListResponse(
        Long id,
        Long patientId,
        String patientName,
        String title,
        JourneyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        CurrentScheduleItemDto currentScheduleItem,
        Integer progress,
        Integer assignedStaffCount
) {
    public static JourneyListResponse from(Journey entity) {
        var items = entity.getScheduleItems();
        long totalItems = items.size();
        long completedItems = items.stream()
                .filter(i -> i.getStatus() == ScheduleItemStatus.COMPLETED)
                .count();
        int progress = totalItems > 0 ? (int) (completedItems * 100 / totalItems) : 0;

        JourneyScheduleItem currentItem = items.stream()
                .filter(i -> i.getStatus() == ScheduleItemStatus.IN_PROGRESS
                        || i.getStatus() == ScheduleItemStatus.EN_ROUTE
                        || i.getStatus() == ScheduleItemStatus.ARRIVED)
                .findFirst()
                .orElse(null);

        CurrentScheduleItemDto currentDto = currentItem != null
                ? new CurrentScheduleItemDto(currentItem.getId(), currentItem.getTitle(), currentItem.getStatus())
                : null;

        long staffCount = items.stream()
                .flatMap(i -> i.getStaffAssignments().stream())
                .map(sa -> sa.getStaff().getId())
                .distinct()
                .count();

        return new JourneyListResponse(
                entity.getId(),
                entity.getPatient().getId(),
                entity.getPatient().getName(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                currentDto,
                progress,
                (int) staffCount
        );
    }

    public record CurrentScheduleItemDto(Long id, String title, ScheduleItemStatus status) {
    }
}
