package com.kmedical.dto.staff;

import com.kmedical.domain.enums.Language;
import com.kmedical.domain.enums.StaffAvailability;

import java.util.List;

/** Staff 도메인 복사 DTO — Interface 계층 노출용 */
public class StaffDTO {

    private String userId;
    private String agencyId;
    private String displayNameEn;
    private String specialization;
    private String profilePhotoUrl;
    private Integer experienceYears;
    private StaffAvailability availabilityStatus;
    private Boolean onboardingComplete;
    private String staffType;
    private String vehicleNumber;
    private String vehicleType;
    private List<Language> interpreterLanguages;

    public StaffDTO() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

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

    public String getStaffType() { return staffType; }
    public void setStaffType(String staffType) { this.staffType = staffType; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public List<Language> getInterpreterLanguages() { return interpreterLanguages; }
    public void setInterpreterLanguages(List<Language> interpreterLanguages) { this.interpreterLanguages = interpreterLanguages; }
}
