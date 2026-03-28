package com.k.medtour.domain.aftercare.repository;

import com.k.medtour.domain.aftercare.entity.AftercareGuide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AftercareGuideRepository extends JpaRepository<AftercareGuide, Long> {

    Optional<AftercareGuide> findByJourneyIdAndDeletedAtIsNull(Long journeyId);

    boolean existsByJourneyIdAndDeletedAtIsNull(Long journeyId);
}
