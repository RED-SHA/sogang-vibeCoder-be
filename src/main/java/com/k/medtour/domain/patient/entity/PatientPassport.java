package com.k.medtour.domain.patient.entity;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.patient.enums.Gender;
import com.k.medtour.domain.patient.enums.PassportInputType;
import com.k.medtour.domain.patient.enums.VerificationStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_passport")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PatientPassport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "passport_number", nullable = false, length = 20)
    private String passportNumber;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "nationality", nullable = false, length = 100)
    private String nationality;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 10)
    private PassportInputType inputType;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "ocr_confidence")
    private Double ocrConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Builder
    public PatientPassport(Member member, String passportNumber, String fullName,
                           String nationality, LocalDate birthDate, LocalDate expiryDate,
                           Gender gender, PassportInputType inputType, Long fileId,
                           Double ocrConfidence) {
        this.member = member;
        this.passportNumber = passportNumber;
        this.fullName = fullName;
        this.nationality = nationality;
        this.birthDate = birthDate;
        this.expiryDate = expiryDate;
        this.gender = gender;
        this.inputType = inputType;
        this.fileId = fileId;
        this.ocrConfidence = ocrConfidence;
        this.verificationStatus = VerificationStatus.PENDING;
    }

    public void update(String passportNumber, String fullName, String nationality,
                       LocalDate birthDate, LocalDate expiryDate, Gender gender,
                       PassportInputType inputType, Long fileId, Double ocrConfidence) {
        this.passportNumber = passportNumber;
        this.fullName = fullName;
        this.nationality = nationality;
        this.birthDate = birthDate;
        this.expiryDate = expiryDate;
        this.gender = gender;
        this.inputType = inputType;
        this.fileId = fileId;
        this.ocrConfidence = ocrConfidence;
        this.verificationStatus = VerificationStatus.PENDING;
        this.verifiedAt = null;
    }

    public void verify() {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void reject() {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.verifiedAt = null;
    }
}
