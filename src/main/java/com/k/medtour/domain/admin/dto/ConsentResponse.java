package com.k.medtour.domain.admin.dto;

import com.k.medtour.domain.admin.entity.MemberConsent;

import java.time.LocalDateTime;

public record ConsentResponse(
        Boolean termsOfService,
        Boolean privacyPolicy,
        Boolean medicalDataConsent,
        Boolean marketingConsent,
        String consentVersion,
        LocalDateTime consentedAt
) {
    public static ConsentResponse from(MemberConsent consent) {
        return new ConsentResponse(
                consent.getTermsOfService(),
                consent.getPrivacyPolicy(),
                consent.getMedicalDataConsent(),
                consent.getMarketingConsent(),
                consent.getConsentVersion(),
                consent.getConsentedAt()
        );
    }
}
