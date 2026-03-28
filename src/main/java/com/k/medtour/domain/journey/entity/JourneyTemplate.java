package com.k.medtour.domain.journey.entity;

import com.k.medtour.domain.journey.enums.TemplateCategory;
import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journey_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JourneyTemplate extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private TemplateCategory category;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<JourneyTemplateItem> items = new ArrayList<>();

    @Builder
    public JourneyTemplate(String name, TemplateCategory category, Integer durationDays) {
        this.name = name;
        this.category = category;
        this.durationDays = durationDays;
        this.usageCount = 0;
    }

    public void update(String name, TemplateCategory category, Integer durationDays) {
        this.name = name;
        this.category = category;
        this.durationDays = durationDays;
    }

    public void incrementUsageCount() {
        this.usageCount++;
    }

    public void replaceItems(List<JourneyTemplateItem> newItems) {
        this.items.clear();
        newItems.forEach(item -> item.assignTemplate(this));
        this.items.addAll(newItems);
    }
}
