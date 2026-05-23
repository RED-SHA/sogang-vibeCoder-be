package com.kmedical.domain.entity;

import com.kmedical.domain.enums.StaffAvailability;

/** C04 — Staff «entity» extends User abstract */
public abstract class Staff extends User {

    private String agencyId;
    private String displayNameEn;
    private String specialization;
    private String profilePhotoUrl;
    private Integer experienceYears;
    private StaffAvailability availabilityStatus;
    private Boolean onboardingComplete;

    protected Staff() { super(); }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getDisplayNameEn() { return displayNameEn; }
    public void setDisplayNameEn(String displayNameEn) { this.displayNameEn = displayNameEn; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public StaffAvailability getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(StaffAvailability availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public Boolean getOnboardingComplete() { return onboardingComplete; }
    public void setOnboardingComplete(Boolean onboardingComplete) { this.onboardingComplete = onboardingComplete; }
}
