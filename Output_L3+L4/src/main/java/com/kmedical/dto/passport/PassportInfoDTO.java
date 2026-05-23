package com.kmedical.dto.passport;

import com.kmedical.domain.enums.PassportReviewStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** PassportInfo 도메인 복사 DTO — Interface 계층 노출용 */
public class PassportInfoDTO {

    private String passportInfoId;
    private String patientId;
    private String imageUrl;
    private String ocrFullNameEn;
    private String ocrPassportNumber;
    private String ocrNationality;
    private LocalDate ocrExpiryDate;
    private BigDecimal ocrConfidence;
    private String confirmedFullNameEn;
    private String confirmedPassportNumber;
    private PassportReviewStatus reviewStatus;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String rejectionReason;

    public PassportInfoDTO() {}

    public String getPassportInfoId() { return passportInfoId; }
    public void setPassportInfoId(String passportInfoId) { this.passportInfoId = passportInfoId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOcrFullNameEn() { return ocrFullNameEn; }
    public void setOcrFullNameEn(String ocrFullNameEn) { this.ocrFullNameEn = ocrFullNameEn; }

    public String getOcrPassportNumber() { return ocrPassportNumber; }
    public void setOcrPassportNumber(String ocrPassportNumber) { this.ocrPassportNumber = ocrPassportNumber; }

    public String getOcrNationality() { return ocrNationality; }
    public void setOcrNationality(String ocrNationality) { this.ocrNationality = ocrNationality; }

    public LocalDate getOcrExpiryDate() { return ocrExpiryDate; }
    public void setOcrExpiryDate(LocalDate ocrExpiryDate) { this.ocrExpiryDate = ocrExpiryDate; }

    public BigDecimal getOcrConfidence() { return ocrConfidence; }
    public void setOcrConfidence(BigDecimal ocrConfidence) { this.ocrConfidence = ocrConfidence; }

    public String getConfirmedFullNameEn() { return confirmedFullNameEn; }
    public void setConfirmedFullNameEn(String confirmedFullNameEn) { this.confirmedFullNameEn = confirmedFullNameEn; }

    public String getConfirmedPassportNumber() { return confirmedPassportNumber; }
    public void setConfirmedPassportNumber(String confirmedPassportNumber) { this.confirmedPassportNumber = confirmedPassportNumber; }

    public PassportReviewStatus getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(PassportReviewStatus reviewStatus) { this.reviewStatus = reviewStatus; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
