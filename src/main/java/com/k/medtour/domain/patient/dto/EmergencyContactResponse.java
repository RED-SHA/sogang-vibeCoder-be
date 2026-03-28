package com.k.medtour.domain.patient.dto;

import com.k.medtour.domain.patient.entity.EmergencyContact;

import java.time.LocalDateTime;

public record EmergencyContactResponse(
        Long id,
        Long memberId,
        String name,
        String relationship,
        String phone,
        String email,
        Boolean isPrimary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EmergencyContactResponse from(EmergencyContact entity) {
        return new EmergencyContactResponse(
                entity.getId(),
                entity.getMember().getId(),
                entity.getName(),
                entity.getRelationship(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getIsPrimary(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
