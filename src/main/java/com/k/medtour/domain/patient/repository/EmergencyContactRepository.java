package com.k.medtour.domain.patient.repository;

import com.k.medtour.domain.patient.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findAllByMemberIdAndDeletedAtIsNull(Long memberId);

    Optional<EmergencyContact> findByIdAndMemberIdAndDeletedAtIsNull(Long id, Long memberId);
}
