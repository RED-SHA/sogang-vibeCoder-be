package com.kmedical.dto.patient;

import com.kmedical.domain.enums.Language;
import com.kmedical.domain.enums.OnboardingStatus;

import java.time.LocalDate;

/** Patient 도메인 복사 DTO — Interface 계층 노출용 */
public class PatientDTO {

    private String userId;
    private String email;
    private String fullNameEn;
    private LocalDate dateOfBirth;
    private String nationality;
    private OnboardingStatus onboardingStatus;
    private Language preferredLanguage;

    public PatientDTO() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullNameEn() { return fullNameEn; }
    public void setFullNameEn(String fullNameEn) { this.fullNameEn = fullNameEn; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public OnboardingStatus getOnboardingStatus() { return onboardingStatus; }
    public void setOnboardingStatus(OnboardingStatus onboardingStatus) { this.onboardingStatus = onboardingStatus; }

    public Language getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(Language preferredLanguage) { this.preferredLanguage = preferredLanguage; }
}
