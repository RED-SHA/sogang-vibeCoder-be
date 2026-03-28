package com.k.medtour.domain.admin.dto;

public record LicenseVerifyResponse(
        Long id,
        String name,
        String licenseNumber,
        Boolean licenseVerified
) {
    public static LicenseVerifyResponse from(com.k.medtour.domain.admin.entity.AgencyProfile profile) {
        return new LicenseVerifyResponse(
                profile.getId(),
                profile.getName(),
                profile.getLicenseNumber(),
                profile.getLicenseVerified()
        );
    }
}
