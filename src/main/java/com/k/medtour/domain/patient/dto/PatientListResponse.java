package com.k.medtour.domain.patient.dto;

import com.k.medtour.domain.admin.entity.Member;

import java.time.LocalDateTime;

public record PatientListResponse(
        Long id,
        String name,
        String email,
        String phone,
        String language,
        boolean hasPassport,
        boolean hasQuestionnaire,
        int emergencyContactCount,
        LocalDateTime createdAt
) {
    public static PatientListResponse of(Member member, boolean hasPassport,
                                          boolean hasQuestionnaire, int emergencyContactCount) {
        return new PatientListResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getLanguage(),
                hasPassport,
                hasQuestionnaire,
                emergencyContactCount,
                member.getCreatedAt()
        );
    }
}
