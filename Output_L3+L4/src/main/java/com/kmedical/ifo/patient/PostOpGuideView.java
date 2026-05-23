package com.kmedical.ifo.patient;

import com.kmedical.control.GuideController;
import com.kmedical.dto.guide.RecoveryGuideDTO;

import java.util.List;

/**
 * IFO-P12 — PostOpGuideView
 * UC: UC-P13 (수술 후 회복 가이드 열람)
 * 책임: 환자가 배포된 회복 가이드를 조회하는 UI 진입점.
 */
public class PostOpGuideView {

    private final GuideController guideController;

    public PostOpGuideView(GuideController guideController) {
        this.guideController = guideController;
    }

    /**
     * 환자가 자신에게 배포된 회복 가이드 목록을 조회한다.
     * Actor Action: Patient views the list of recovery guides shared with them.
     */
    public List<RecoveryGuideDTO> viewMyGuides(String patientId) {
        return guideController.getGuidesForPatient(patientId);
    }

    /**
     * 환자가 특정 회복 가이드를 열람한다.
     * Actor Action: Patient opens and reads a recovery guide.
     */
    public RecoveryGuideDTO openGuide(String guideId) {
        return guideController.getGuide(guideId);
    }
}
