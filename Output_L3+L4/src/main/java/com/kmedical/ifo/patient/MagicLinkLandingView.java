package com.kmedical.ifo.patient;

import com.kmedical.control.AccessLinkController;
import com.kmedical.dto.accesslink.AccessLinkVerifyRequestDTO;
import com.kmedical.dto.accesslink.AccessLinkVerifyResponseDTO;

/**
 * IFO-P01 — MagicLinkLandingView
 * UC: UC-P01 (매직 링크 인증)
 * 책임: 환자가 이메일/SMS로 수신한 매직 링크를 통해 접속하여 인증하는 UI 진입점.
 */
public class MagicLinkLandingView {

    private final AccessLinkController accessLinkController;

    public MagicLinkLandingView(AccessLinkController accessLinkController) {
        this.accessLinkController = accessLinkController;
    }

    /**
     * 환자가 매직 링크 토큰과 생년월일로 본인 인증을 시도한다.
     * Actor Action: Patient submits the magic link token and date of birth.
     */
    public AccessLinkVerifyResponseDTO verifyMagicLink(AccessLinkVerifyRequestDTO request) {
        return accessLinkController.verifyAccessLink(request);
    }
}
