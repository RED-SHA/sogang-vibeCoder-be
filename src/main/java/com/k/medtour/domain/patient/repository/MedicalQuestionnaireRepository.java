package com.k.medtour.domain.patient.repository;

import com.k.medtour.domain.patient.entity.MedicalQuestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalQuestionnaireRepository extends JpaRepository<MedicalQuestionnaire, Long> {

    Optional<MedicalQuestionnaire> findByMemberIdAndDeletedAtIsNull(Long memberId);

    boolean existsByMemberIdAndDeletedAtIsNull(Long memberId);
}
