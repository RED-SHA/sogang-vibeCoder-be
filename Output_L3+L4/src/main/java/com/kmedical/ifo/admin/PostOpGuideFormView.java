package com.kmedical.ifo.admin;

import com.kmedical.control.GuideController;
import com.kmedical.dto.guide.GuideCreateRequestDTO;
import com.kmedical.dto.guide.GuideDeliveryRequestDTO;
import com.kmedical.dto.guide.RecoveryGuideDTO;

/**
 * IFO-A11 — PostOpGuideFormView
 * UC: UC-A10 (수술 후 회복 가이드 등록·배포)
 * 책임: 관리자가 회복 가이드를 작성하고 환자에게 배포하는 UI 진입점.
 */
public class PostOpGuideFormView {

    private final GuideController guideController;

    public PostOpGuideFormView(GuideController guideController) {
        this.guideController = guideController;
    }

    /**
     * 관리자가 회복 가이드를 작성·저장한다.
     * Actor Action: Admin creates a new recovery guide.
     */
    public RecoveryGuideDTO createGuide(GuideCreateRequestDTO request) {
        return guideController.createGuide(request);
    }

    /**
     * 관리자가 회복 가이드를 환자에게 배포한다.
     * Actor Action: Admin delivers the guide to a patient.
     */
    public void deliverGuide(GuideDeliveryRequestDTO request) {
        guideController.deliverGuide(request);
    }

    /**
     * 관리자가 특정 가이드를 조회한다.
     * Actor Action: Admin views a recovery guide.
     */
    public RecoveryGuideDTO viewGuide(String guideId) {
        return guideController.getGuide(guideId);
    }
}
