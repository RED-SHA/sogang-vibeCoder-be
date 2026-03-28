package com.k.medtour.domain.journey.repository;

import com.k.medtour.domain.journey.entity.Journey;
import com.k.medtour.domain.journey.enums.JourneyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JourneyRepository extends JpaRepository<Journey, Long> {

    Optional<Journey> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT j FROM Journey j WHERE j.deletedAt IS NULL " +
            "AND (:status IS NULL OR j.status = :status) " +
            "AND (:patientId IS NULL OR j.patient.id = :patientId) " +
            "AND (:startDateFrom IS NULL OR j.startDate >= :startDateFrom) " +
            "AND (:startDateTo IS NULL OR j.startDate <= :startDateTo)")
    Page<Journey> findAllByFilters(
            @Param("status") JourneyStatus status,
            @Param("patientId") Long patientId,
            @Param("startDateFrom") LocalDate startDateFrom,
            @Param("startDateTo") LocalDate startDateTo,
            Pageable pageable
    );

    boolean existsByPatientIdAndStatusIn(Long patientId, List<JourneyStatus> statuses);

    @Query("SELECT j FROM Journey j WHERE j.patient.id = :patientId " +
            "AND j.status IN :statuses AND j.deletedAt IS NULL " +
            "ORDER BY j.startDate DESC")
    List<Journey> findByPatientIdAndStatusIn(
            @Param("patientId") Long patientId,
            @Param("statuses") List<JourneyStatus> statuses
    );
}
