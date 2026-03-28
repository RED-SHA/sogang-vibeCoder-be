package com.k.medtour.domain.aftercare.entity;

import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_report", indexes = {
        @Index(name = "idx_staff_report_journey_id", columnList = "journey_id"),
        @Index(name = "idx_staff_report_staff_id", columnList = "staff_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StaffReport extends BaseEntity {

    @Column(name = "journey_id", nullable = false)
    private Long journeyId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "report_content", columnDefinition = "TEXT", nullable = false)
    private String reportContent;

    @Column(name = "work_hours", nullable = false)
    private Double workHours;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public StaffReport(Long journeyId, Long staffId, String reportContent,
                       Double workHours) {
        this.journeyId = journeyId;
        this.staffId = staffId;
        this.reportContent = reportContent;
        this.workHours = workHours;
        this.completedAt = LocalDateTime.now();
    }
}
