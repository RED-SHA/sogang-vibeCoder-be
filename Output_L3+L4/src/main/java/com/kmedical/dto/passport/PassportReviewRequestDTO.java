package com.kmedical.dto.passport;

import com.kmedical.domain.enums.PassportReviewStatus;

/** Interface → PassportController 간 여권 OCR 검토 요청 DTO */
public class PassportReviewRequestDTO {

    private String passportInfoId;
    private String reviewedBy;
    private String confirmedFullNameEn;
    private String confirmedPassportNumber;
    private PassportReviewStatus reviewStatus;
    private String rejectionReason;

    public PassportReviewRequestDTO() {}

    public String getPassportInfoId() { return passportInfoId; }
    public void setPassportInfoId(String passportInfoId) { this.passportInfoId = passportInfoId; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getConfirmedFullNameEn() { return confirmedFullNameEn; }
    public void setConfirmedFullNameEn(String confirmedFullNameEn) { this.confirmedFullNameEn = confirmedFullNameEn; }

    public String getConfirmedPassportNumber() { return confirmedPassportNumber; }
    public void setConfirmedPassportNumber(String confirmedPassportNumber) { this.confirmedPassportNumber = confirmedPassportNumber; }

    public PassportReviewStatus getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(PassportReviewStatus reviewStatus) { this.reviewStatus = reviewStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
