package com.k.medtour.domain.aftercare.repository;

import com.k.medtour.domain.aftercare.entity.StaffReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffReportRepository extends JpaRepository<StaffReport, Long> {

    boolean existsByJourneyIdAndStaffIdAndDeletedAtIsNull(Long journeyId, Long staffId);

    Optional<StaffReport> findByJourneyIdAndStaffIdAndDeletedAtIsNull(Long journeyId, Long staffId);
}
