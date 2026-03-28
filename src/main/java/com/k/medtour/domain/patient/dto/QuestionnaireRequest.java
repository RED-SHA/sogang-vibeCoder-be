package com.k.medtour.domain.patient.dto;

import com.k.medtour.domain.patient.enums.BloodType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuestionnaireRequest(
        @NotNull(message = "혈액형은 필수입니다.")
        BloodType bloodType,

        Double height,

        Double weight,

        List<String> allergies,

        List<String> currentMedications,

        List<String> pastSurgeries,

        List<String> chronicConditions,

        String additionalNotes
) {
}
