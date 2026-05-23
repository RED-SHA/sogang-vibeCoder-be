package com.kmedical.ifo.staff;

import com.kmedical.control.WorkController;
import com.kmedical.dto.staff.WorkProofPhotoDTO;

/**
 * IFO-S06 — ProofPhotoUploadForm
 * UC: UC-S06 (업무 증명 사진 업로드)
 * 책임: 스태프가 업무 완료 증명 사진을 업로드하는 UI 진입점.
 *       사진 보관 기한(retentionExpiresAt)은 WorkController에서 자동 설정된다.
 */
public class ProofPhotoUploadForm {

    private final WorkController workController;

    public ProofPhotoUploadForm(WorkController workController) {
        this.workController = workController;
    }

    /**
     * 스태프가 업무 증명 사진을 업로드한다.
     * Actor Action: Staff uploads a work proof photo for an assignment.
     */
    public WorkProofPhotoDTO uploadProofPhoto(WorkProofPhotoDTO dto) {
        return workController.uploadProofPhoto(dto);
    }
}
