package com.k.medtour.domain.journey.entity;

import com.k.medtour.domain.journey.enums.ScheduleItemStatus;
import com.k.medtour.domain.journey.enums.ScheduleItemType;
import com.k.medtour.global.common.BaseEntity;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "journey_schedule_item", indexes = {
        @Index(name = "idx_schedule_item_journey_id", columnList = "journey_id"),
        @Index(name = "idx_schedule_item_scheduled_at", columnList = "scheduled_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JourneyScheduleItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ScheduleItemType type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ScheduleItemStatus status = ScheduleItemStatus.SCHEDULED;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "location", columnDefinition = "jsonb")
    private Map<String, Object> location;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "scheduleItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffAssignment> staffAssignments = new ArrayList<>();

    @Builder
    public JourneyScheduleItem(Journey journey, Integer dayNumber, LocalDateTime scheduledAt,
                                String title, ScheduleItemType type, String description,
                                Integer durationMinutes, Map<String, Object> location) {
        this.journey = journey;
        this.dayNumber = dayNumber;
        this.scheduledAt = scheduledAt;
        this.title = title;
        this.type = type;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.location = location;
        this.status = ScheduleItemStatus.SCHEDULED;
    }

    public void updateStatus(ScheduleItemStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "상태 전이 불가: " + this.status + " -> " + newStatus);
        }
        this.status = newStatus;
        if (newStatus == ScheduleItemStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public void update(LocalDateTime scheduledAt, String title, String description,
                       Integer durationMinutes, Map<String, Object> location) {
        if (this.status == ScheduleItemStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.SCHEDULE_ITEM_COMPLETED,
                    "이미 완료된 일정은 수정할 수 없습니다.");
        }
        if (scheduledAt != null) this.scheduledAt = scheduledAt;
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (durationMinutes != null) this.durationMinutes = durationMinutes;
        if (location != null) this.location = location;
    }

    public boolean isModifiable() {
        return this.status != ScheduleItemStatus.COMPLETED
                && this.status != ScheduleItemStatus.IN_PROGRESS
                && this.status != ScheduleItemStatus.CANCELLED;
    }
}
