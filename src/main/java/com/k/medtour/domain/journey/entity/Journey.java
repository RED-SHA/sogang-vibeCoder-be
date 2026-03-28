package com.k.medtour.domain.journey.entity;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.journey.enums.JourneyStatus;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journey", indexes = {
        @Index(name = "idx_journey_patient_id", columnList = "patient_id"),
        @Index(name = "idx_journey_status", columnList = "status"),
        @Index(name = "idx_journey_start_date", columnList = "start_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Journey extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Member patient;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JourneyStatus status = JourneyStatus.PLANNED;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("scheduledAt ASC")
    private List<JourneyScheduleItem> scheduleItems = new ArrayList<>();

    @Builder
    public Journey(Member patient, String title, LocalDate startDate, LocalDate endDate, String notes) {
        this.patient = patient;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
        this.status = JourneyStatus.PLANNED;
    }

    public void start() {
        if (this.status != JourneyStatus.PLANNED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "여정을 시작할 수 없는 상태입니다: " + this.status);
        }
        this.status = JourneyStatus.IN_PROGRESS;
    }

    public void complete() {
        if (this.status != JourneyStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "여정을 완료할 수 없는 상태입니다: " + this.status);
        }
        this.status = JourneyStatus.COMPLETED;
    }

    public void cancel() {
        if (this.status == JourneyStatus.COMPLETED || this.status == JourneyStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "여정을 취소할 수 없는 상태입니다: " + this.status);
        }
        this.status = JourneyStatus.CANCELLED;
    }

    public void addScheduleItem(JourneyScheduleItem item) {
        this.scheduleItems.add(item);
    }
}
