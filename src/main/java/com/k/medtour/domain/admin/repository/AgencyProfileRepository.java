package com.k.medtour.domain.admin.repository;

import com.k.medtour.domain.admin.entity.AgencyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgencyProfileRepository extends JpaRepository<AgencyProfile, Long> {

    Optional<AgencyProfile> findByLicenseNumber(String licenseNumber);
}
