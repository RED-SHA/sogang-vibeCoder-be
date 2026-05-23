package com.kmedical.ifo.patient;

import com.kmedical.control.PassportController;
import com.kmedical.dto.passport.PassportInfoDTO;
import com.kmedical.dto.passport.PassportUploadRequestDTO;

/**
 * IFO-P03 — PassportUploadForm
 * UC: UC-P03 (여권 업로드)
 * 책임: 환자가 여권 사진을 업로드하고 OCR 결과를 확인하는 UI 진입점.
 */
public class PassportUploadForm {

    private final PassportController passportController;

    public PassportUploadForm(PassportController passportController) {
        this.passportController = passportController;
    }

    /**
     * 환자가 여권 이미지를 업로드한다.
     * Actor Action: Patient uploads a passport image for OCR processing.
     */
    public PassportInfoDTO uploadPassport(PassportUploadRequestDTO request) {
        return passportController.uploadAndOcr(request);
    }

    /**
     * 환자가 OCR 결과를 조회한다.
     * Actor Action: Patient views the OCR-extracted passport information.
     */
    public PassportInfoDTO viewPassportInfo(String passportInfoId) {
        return passportController.getPassportInfo(passportInfoId);
    }
}
