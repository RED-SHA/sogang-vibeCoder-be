package com.k.medtour.domain.patient.dto;

import com.k.medtour.domain.patient.entity.MedicalQuestionnaire;
import com.k.medtour.domain.patient.enums.BloodType;
import com.k.medtour.domain.patient.enums.QuestionnaireStatus;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionnaireResponse(
        Long id,
        Long memberId,
        BloodType bloodType,
        Double height,
        Double weight,
        List<String> allergies,
        List<String> currentMedications,
        List<String> pastSurgeries,
        List<String> chronicConditions,
        String additionalNotes,
        QuestionnaireStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static QuestionnaireResponse from(MedicalQuestionnaire entity) {
        return new QuestionnaireResponse(
                entity.getId(),
                entity.getMember().getId(),
                entity.getBloodType(),
                entity.getHeight(),
                entity.getWeight(),
                entity.getAllergies(),
                entity.getCurrentMedications(),
                entity.getPastSurgeries(),
                entity.getChronicConditions(),
                entity.getAdditionalNotes(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
