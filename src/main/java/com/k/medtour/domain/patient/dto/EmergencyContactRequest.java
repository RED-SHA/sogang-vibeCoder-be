package com.k.medtour.domain.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmergencyContactRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "관계는 필수입니다.")
        String relationship,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phone,

        String email,

        @NotNull(message = "주 연락처 여부는 필수입니다.")
        Boolean isPrimary
) {
}
