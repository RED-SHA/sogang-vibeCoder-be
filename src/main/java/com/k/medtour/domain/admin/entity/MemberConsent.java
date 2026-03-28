package com.k.medtour.domain.admin.entity;

import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_consent")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberConsent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "terms_of_service", nullable = false)
    private Boolean termsOfService = false;

    @Column(name = "privacy_policy", nullable = false)
    private Boolean privacyPolicy = false;

    @Column(name = "medical_data_consent", nullable = false)
    private Boolean medicalDataConsent = false;

    @Column(name = "marketing_consent", nullable = false)
    private Boolean marketingConsent = false;

    @Column(name = "consent_version", nullable = false, length = 20)
    private String consentVersion;

    @Column(name = "consented_at", nullable = false)
    private LocalDateTime consentedAt;

    @Builder
    public MemberConsent(Member member, Boolean termsOfService, Boolean privacyPolicy,
                         Boolean medicalDataConsent, Boolean marketingConsent,
                         String consentVersion, LocalDateTime consentedAt) {
        this.member = member;
        this.termsOfService = termsOfService != null ? termsOfService : false;
        this.privacyPolicy = privacyPolicy != null ? privacyPolicy : false;
        this.medicalDataConsent = medicalDataConsent != null ? medicalDataConsent : false;
        this.marketingConsent = marketingConsent != null ? marketingConsent : false;
        this.consentVersion = consentVersion;
        this.consentedAt = consentedAt != null ? consentedAt : LocalDateTime.now();
    }
}
