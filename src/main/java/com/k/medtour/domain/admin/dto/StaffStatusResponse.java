package com.k.medtour.domain.admin.dto;

import java.util.List;

public record StaffStatusResponse(
        long totalStaff,
        long available,
        long onDuty,
        long offline,
        List<StaffStatusItem> staffList
) {
    public record StaffStatusItem(
            Long memberId,
            String name,
            String staffType,
            String status
    ) {
    }
}
