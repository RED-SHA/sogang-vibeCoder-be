package com.k.medtour.domain.admin.dto;

import com.k.medtour.domain.admin.entity.AgencyProfile;

import java.time.LocalDateTime;

public record AgencyProfileResponse(
        Long id,
        String name,
        String licenseNumber,
        Boolean licenseVerified,
        String address,
        String phone,
        String website,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AgencyProfileResponse from(AgencyProfile profile) {
        return new AgencyProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getLicenseNumber(),
                profile.getLicenseVerified(),
                profile.getAddress(),
                profile.getPhone(),
                profile.getWebsite(),
                profile.getDescription(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
