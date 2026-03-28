package com.k.medtour.domain.patient.dto;

import com.k.medtour.domain.patient.enums.Gender;
import com.k.medtour.domain.patient.enums.PassportInputType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PassportRequest(
        @NotBlank(message = "여권번호는 필수입니다.")
        String passportNumber,

        @NotBlank(message = "성명은 필수입니다.")
        String fullName,

        @NotBlank(message = "국적은 필수입니다.")
        String nationality,

        @NotNull(message = "생년월일은 필수입니다.")
        LocalDate birthDate,

        @NotNull(message = "여권 만료일은 필수입니다.")
        LocalDate expiryDate,

        @NotNull(message = "성별은 필수입니다.")
        Gender gender,

        @NotNull(message = "입력 방식은 필수입니다.")
        PassportInputType inputType,

        Long fileId,

        Double ocrConfidence
) {
}
