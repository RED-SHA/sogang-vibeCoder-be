package com.k.medtour.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsentRequest(
        @NotNull(message = "termsOfService는 필수입니다.")
        Boolean termsOfService,

        @NotNull(message = "privacyPolicy는 필수입니다.")
        Boolean privacyPolicy,

        @NotNull(message = "medicalDataConsent는 필수입니다.")
        Boolean medicalDataConsent,

        Boolean marketingConsent,

        @NotBlank(message = "consentVersion은 필수입니다.")
        String consentVersion
) {
}
