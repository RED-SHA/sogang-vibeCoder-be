package com.k.medtour.domain.patient.entity;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.patient.enums.BloodType;
import com.k.medtour.domain.patient.enums.QuestionnaireStatus;
import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

@Entity
@Table(name = "medical_questionnaire")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicalQuestionnaire extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", nullable = false, length = 20)
    private BloodType bloodType;

    @Column(name = "height")
    private Double height;

    @Column(name = "weight")
    private Double weight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allergies", columnDefinition = "jsonb")
    private List<String> allergies;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "current_medications", columnDefinition = "jsonb")
    private List<String> currentMedications;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "past_surgeries", columnDefinition = "jsonb")
    private List<String> pastSurgeries;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chronic_conditions", columnDefinition = "jsonb")
    private List<String> chronicConditions;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuestionnaireStatus status = QuestionnaireStatus.SUBMITTED;

    @Builder
    public MedicalQuestionnaire(Member member, BloodType bloodType, Double height, Double weight,
                                 List<String> allergies, List<String> currentMedications,
                                 List<String> pastSurgeries, List<String> chronicConditions,
                                 String additionalNotes) {
        this.member = member;
        this.bloodType = bloodType;
        this.height = height;
        this.weight = weight;
        this.allergies = allergies;
        this.currentMedications = currentMedications;
        this.pastSurgeries = pastSurgeries;
        this.chronicConditions = chronicConditions;
        this.additionalNotes = additionalNotes;
        this.status = QuestionnaireStatus.SUBMITTED;
    }

    public void update(BloodType bloodType, Double height, Double weight,
                       List<String> allergies, List<String> currentMedications,
                       List<String> pastSurgeries, List<String> chronicConditions,
                       String additionalNotes) {
        this.bloodType = bloodType;
        this.height = height;
        this.weight = weight;
        this.allergies = allergies;
        this.currentMedications = currentMedications;
        this.pastSurgeries = pastSurgeries;
        this.chronicConditions = chronicConditions;
        this.additionalNotes = additionalNotes;
        this.status = QuestionnaireStatus.SUBMITTED;
    }

    public void markReviewed() {
        this.status = QuestionnaireStatus.REVIEWED;
    }
}
