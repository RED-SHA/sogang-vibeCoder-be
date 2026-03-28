package com.k.medtour.domain.patient.dto;

import com.k.medtour.domain.patient.entity.PatientPassport;
import com.k.medtour.domain.patient.enums.Gender;
import com.k.medtour.domain.patient.enums.PassportInputType;
import com.k.medtour.domain.patient.enums.VerificationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PassportResponse(
        Long id,
        Long memberId,
        String passportNumber,
        String fullName,
        String nationality,
        LocalDate birthDate,
        LocalDate expiryDate,
        Gender gender,
        PassportInputType inputType,
        Long fileId,
        Double ocrConfidence,
        VerificationStatus verificationStatus,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PassportResponse from(PatientPassport entity) {
        return new PassportResponse(
                entity.getId(),
                entity.getMember().getId(),
                maskPassportNumber(entity.getPassportNumber()),
                entity.getFullName(),
                entity.getNationality(),
                entity.getBirthDate(),
                entity.getExpiryDate(),
                entity.getGender(),
                entity.getInputType(),
                entity.getFileId(),
                entity.getOcrConfidence(),
                entity.getVerificationStatus(),
                entity.getVerifiedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static String maskPassportNumber(String passportNumber) {
        if (passportNumber == null || passportNumber.length() <= 4) {
            return "****";
        }
        int visibleLength = passportNumber.length() - 4;
        return passportNumber.substring(0, visibleLength) + "****";
    }
}
