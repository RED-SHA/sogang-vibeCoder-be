package com.k.medtour.domain.journey.dto;

import com.k.medtour.domain.journey.enums.ScheduleItemStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull ScheduleItemStatus status,
        String note
) {
}
