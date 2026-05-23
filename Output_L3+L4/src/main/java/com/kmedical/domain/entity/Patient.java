package com.kmedical.domain.entity;

import com.kmedical.domain.enums.OnboardingStatus;

import java.time.LocalDate;

/** C03 — Patient «entity» extends User */
public class Patient extends User {

    private String fullNameEn;
    private LocalDate dateOfBirth;
    private String nationality;
    private OnboardingStatus onboardingStatus;

    public Patient() { super(); }

    public String getFullNameEn() { return fullNameEn; }
    public void setFullNameEn(String fullNameEn) { this.fullNameEn = fullNameEn; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public OnboardingStatus getOnboardingStatus() { return onboardingStatus; }
    public void setOnboardingStatus(OnboardingStatus onboardingStatus) { this.onboardingStatus = onboardingStatus; }
}
