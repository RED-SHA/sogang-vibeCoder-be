package com.k.medtour.domain.admin.entity;

import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agency_profile", indexes = {
        @Index(name = "idx_agency_profile_license_number", columnList = "license_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgencyProfile extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "license_number", nullable = false, unique = true, length = 100)
    private String licenseNumber;

    @Column(name = "license_verified", nullable = false)
    private Boolean licenseVerified = false;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "website", length = 500)
    private String website;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder
    public AgencyProfile(String name, String licenseNumber, Boolean licenseVerified,
                         String address, String phone, String website, String description) {
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.licenseVerified = licenseVerified != null ? licenseVerified : false;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.description = description;
    }

    public void updateProfile(String name, String address, String phone,
                              String website, String description) {
        if (name != null) this.name = name;
        if (address != null) this.address = address;
        if (phone != null) this.phone = phone;
        if (website != null) this.website = website;
        if (description != null) this.description = description;
    }

    public void verifyLicense() {
        this.licenseVerified = true;
    }

    public void unverifyLicense() {
        this.licenseVerified = false;
    }
}
