package com.k.medtour.domain.journey.entity;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.journey.enums.StaffAssignmentStatus;
import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_assignment", indexes = {
        @Index(name = "idx_staff_assignment_schedule_item_id", columnList = "schedule_item_id"),
        @Index(name = "idx_staff_assignment_staff_id", columnList = "staff_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StaffAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_item_id", nullable = false)
    private JourneyScheduleItem scheduleItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Member staff;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StaffAssignmentStatus status = StaffAssignmentStatus.ASSIGNED;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public StaffAssignment(JourneyScheduleItem scheduleItem, Member staff) {
        this.scheduleItem = scheduleItem;
        this.staff = staff;
        this.status = StaffAssignmentStatus.ASSIGNED;
        this.assignedAt = LocalDateTime.now();
    }

    public void startDuty() {
        this.status = StaffAssignmentStatus.ON_DUTY;
    }

    public void complete() {
        this.status = StaffAssignmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
