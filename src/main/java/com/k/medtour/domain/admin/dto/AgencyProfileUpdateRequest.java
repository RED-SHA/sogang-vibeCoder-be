package com.k.medtour.domain.admin.dto;

public record AgencyProfileUpdateRequest(
        String name,
        String address,
        String phone,
        String website,
        String description
) {
}
