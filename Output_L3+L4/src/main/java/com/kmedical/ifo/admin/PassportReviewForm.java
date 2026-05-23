package com.kmedical.ifo.admin;

import com.kmedical.control.PassportController;
import com.kmedical.domain.enums.PassportReviewStatus;
import com.kmedical.dto.passport.PassportInfoDTO;
import com.kmedical.dto.passport.PassportReviewRequestDTO;

/**
 * IFO-A04 — PassportReviewForm
 * UC: UC-A03 / UC-X03 (여권 검토)
 * 책임: 관리자가 OCR 결과를 검토하고 APPROVED 또는 REJECTED로 처리하는 UI 진입점.
 */
public class PassportReviewForm {

    private final PassportController passportController;

    public PassportReviewForm(PassportController passportController) {
        this.passportController = passportController;
    }

    /**
     * 관리자가 여권 정보를 조회한다.
     * Actor Action: Admin views passport OCR result for review.
     */
    public PassportInfoDTO viewPassportInfo(String passportInfoId) {
        return passportController.getPassportInfo(passportInfoId);
    }

    /**
     * 관리자가 여권을 승인한다.
     * Actor Action: Admin approves passport information.
     */
    public PassportInfoDTO approvePassport(String passportInfoId, String reviewedBy, String notes) {
        PassportReviewRequestDTO request = new PassportReviewRequestDTO();
        request.setPassportInfoId(passportInfoId);
        request.setReviewStatus(PassportReviewStatus.APPROVED);
        request.setReviewedBy(reviewedBy);
        return passportController.reviewPassport(request);
    }

    /**
     * 관리자가 여권을 반려한다.
     * Actor Action: Admin rejects passport information.
     */
    public PassportInfoDTO rejectPassport(String passportInfoId, String reviewedBy, String notes) {
        PassportReviewRequestDTO request = new PassportReviewRequestDTO();
        request.setPassportInfoId(passportInfoId);
        request.setReviewStatus(PassportReviewStatus.REJECTED);
        request.setReviewedBy(reviewedBy);
        request.setRejectionReason(notes);
        return passportController.reviewPassport(request);
    }
}
