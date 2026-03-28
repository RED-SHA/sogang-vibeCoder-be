package com.k.medtour.domain.admin.dto;

import java.util.List;
import java.util.Map;

public record StaffProfileUpdateRequest(
        String name,
        String phone,
        List<String> languages,
        String profileImageUrl,
        Map<String, Object> vehicleInfo
) {
}
