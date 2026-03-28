package com.k.medtour.domain.journey.dto;

import com.k.medtour.domain.journey.entity.Journey;
import com.k.medtour.domain.journey.entity.JourneyScheduleItem;
import com.k.medtour.domain.journey.enums.JourneyStatus;
import com.k.medtour.domain.journey.enums.ScheduleItemStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record JourneyDetailResponse(
        Long id,
        Long patientId,
        String patientName,
        String title,
        JourneyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String notes,
        List<ScheduleItemDetailDto> scheduleItems,
        Integer progress,
        LocalDateTime createdAt
) {
    public static JourneyDetailResponse from(Journey entity) {
        var items = entity.getScheduleItems();
        long total = items.size();
        long completed = items.stream()
                .filter(i -> i.getStatus() == ScheduleItemStatus.COMPLETED)
                .count();
        int progress = total > 0 ? (int) (completed * 100 / total) : 0;

        List<ScheduleItemDetailDto> scheduleItemDtos = items.stream()
                .map(ScheduleItemDetailDto::from)
                .toList();

        return new JourneyDetailResponse(
                entity.getId(),
                entity.getPatient().getId(),
                entity.getPatient().getName(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getNotes(),
                scheduleItemDtos,
                progress,
                entity.getCreatedAt()
        );
    }

    public record ScheduleItemDetailDto(
            Long id,
            Integer dayNumber,
            LocalDateTime scheduledAt,
            String title,
            String type,
            String description,
            ScheduleItemStatus status,
            Integer durationMinutes,
            LocationDto location,
            List<AssignedStaffDto> assignedStaff,
            LocalDateTime completedAt
    ) {
        public static ScheduleItemDetailDto from(JourneyScheduleItem item) {
            List<AssignedStaffDto> staffDtos = item.getStaffAssignments().stream()
                    .map(sa -> new AssignedStaffDto(
                            sa.getStaff().getId(),
                            sa.getStaff().getName(),
                            null,
                            sa.getStatus().name()
                    ))
                    .toList();

            return new ScheduleItemDetailDto(
                    item.getId(),
                    item.getDayNumber(),
                    item.getScheduledAt(),
                    item.getTitle(),
                    item.getType().name(),
                    item.getDescription(),
                    item.getStatus(),
                    item.getDurationMinutes(),
                    LocationDto.from(item.getLocation()),
                    staffDtos,
                    item.getCompletedAt()
            );
        }
    }

    public record AssignedStaffDto(Long staffId, String name, String staffType, String status) {
    }
}
