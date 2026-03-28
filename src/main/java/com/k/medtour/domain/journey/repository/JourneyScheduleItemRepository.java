package com.k.medtour.domain.journey.repository;

import com.k.medtour.domain.journey.entity.JourneyScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JourneyScheduleItemRepository extends JpaRepository<JourneyScheduleItem, Long> {

    List<JourneyScheduleItem> findByJourneyIdOrderByScheduledAtAsc(Long journeyId);

    @Query("SELECT si FROM JourneyScheduleItem si WHERE si.journey.id = :journeyId " +
            "AND si.scheduledAt >= :startOfDay AND si.scheduledAt < :endOfDay " +
            "ORDER BY si.scheduledAt ASC")
    List<JourneyScheduleItem> findByJourneyIdAndDate(
            @Param("journeyId") Long journeyId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    Optional<JourneyScheduleItem> findByIdAndJourneyId(Long id, Long journeyId);
}
