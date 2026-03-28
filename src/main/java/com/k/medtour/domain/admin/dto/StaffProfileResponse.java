package com.k.medtour.domain.admin.dto;

import com.k.medtour.domain.staff.entity.StaffProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record StaffProfileResponse(
        Long id,
        String name,
        String email,
        String phone,
        String role,
        String staffType,
        List<String> languages,
        String profileImageUrl,
        Map<String, Object> vehicleInfo,
        Boolean isAvailable,
        LocalDateTime createdAt
) {
    public static StaffProfileResponse from(StaffProfile staffProfile) {
        var member = staffProfile.getMember();
        return new StaffProfileResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getRole().getName(),
                staffProfile.getStaffType().name(),
                staffProfile.getLanguages(),
                member.getProfileImage(),
                staffProfile.getVehicleInfo(),
                staffProfile.getIsAvailable(),
                staffProfile.getCreatedAt()
        );
    }
}
