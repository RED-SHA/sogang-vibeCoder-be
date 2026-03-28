package com.k.medtour.domain.journey.repository;

import com.k.medtour.domain.journey.entity.StaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {

    List<StaffAssignment> findByScheduleItemId(Long scheduleItemId);

    @Query("SELECT sa FROM StaffAssignment sa " +
            "JOIN FETCH sa.scheduleItem si " +
            "JOIN FETCH si.journey j " +
            "JOIN FETCH j.patient " +
            "WHERE sa.staff.id = :staffId " +
            "AND si.scheduledAt >= :startOfDay AND si.scheduledAt < :endOfDay " +
            "ORDER BY si.scheduledAt ASC")
    List<StaffAssignment> findByStaffIdAndDate(
            @Param("staffId") Long staffId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    boolean existsByScheduleItemIdAndStaffId(Long scheduleItemId, Long staffId);

    Optional<StaffAssignment> findByScheduleItemIdAndStaffId(Long scheduleItemId, Long staffId);

    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END " +
            "FROM StaffAssignment sa " +
            "JOIN sa.scheduleItem si " +
            "WHERE si.journey.id = :journeyId AND sa.staff.id = :staffId")
    boolean existsByJourneyIdAndStaffId(
            @Param("journeyId") Long journeyId,
            @Param("staffId") Long staffId
    );
}
