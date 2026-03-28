package com.k.medtour.domain.journey.entity;

import com.k.medtour.domain.journey.enums.ScheduleItemType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "journey_template_item", indexes = {
        @Index(name = "idx_template_item_template_id", columnList = "template_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JourneyTemplateItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private JourneyTemplate template;

    @Column(name = "day_offset", nullable = false)
    private Integer dayOffset;

    @Column(name = "time_offset", nullable = false, length = 5)
    private String timeOffset;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ScheduleItemType type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "location", columnDefinition = "jsonb")
    private Map<String, Object> location;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_staff", columnDefinition = "jsonb")
    private List<String> requiredStaff;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Builder
    public JourneyTemplateItem(Integer dayOffset, String timeOffset, String title,
                                ScheduleItemType type, String description, Integer durationMinutes,
                                Map<String, Object> location, List<String> requiredStaff, Integer sortOrder) {
        this.dayOffset = dayOffset;
        this.timeOffset = timeOffset;
        this.title = title;
        this.type = type;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.location = location;
        this.requiredStaff = requiredStaff;
        this.sortOrder = sortOrder;
    }

    public void assignTemplate(JourneyTemplate template) {
        this.template = template;
    }
}
