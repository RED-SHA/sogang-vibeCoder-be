package com.k.medtour.domain.patient.repository;

import com.k.medtour.domain.patient.entity.PatientPassport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientPassportRepository extends JpaRepository<PatientPassport, Long> {

    Optional<PatientPassport> findByMemberIdAndDeletedAtIsNull(Long memberId);

    boolean existsByMemberIdAndDeletedAtIsNull(Long memberId);
}
