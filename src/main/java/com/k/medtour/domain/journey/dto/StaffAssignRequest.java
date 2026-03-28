package com.k.medtour.domain.journey.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StaffAssignRequest(
        @NotNull Long staffId,
        @NotEmpty List<Long> scheduleItemIds
) {
}
