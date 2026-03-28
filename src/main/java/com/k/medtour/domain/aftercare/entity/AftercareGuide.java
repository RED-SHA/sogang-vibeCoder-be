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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "aftercare_guide", indexes = {
        @Index(name = "idx_aftercare_guide_journey_id", columnList = "journey_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AftercareGuide extends BaseEntity {

    @Column(name = "journey_id", nullable = false)
    private Long journeyId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "instructions", columnDefinition = "jsonb")
    private Map<String, Object> instructions;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Builder
    public AftercareGuide(Long journeyId, String title, String content,
                          Map<String, Object> instructions) {
        this.journeyId = journeyId;
        this.title = title;
        this.content = content;
        this.instructions = instructions;
        this.publishedAt = LocalDateTime.now();
    }
}
