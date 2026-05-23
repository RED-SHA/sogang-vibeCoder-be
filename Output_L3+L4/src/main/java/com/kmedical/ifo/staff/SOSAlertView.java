package com.kmedical.ifo.staff;

import com.kmedical.control.SOSController;
import com.kmedical.dto.sos.SOSAlertDTO;
import com.kmedical.dto.sos.SOSAlertRequestDTO;

/**
 * IFO-S09 — SOSAlertView
 * UC: UC-S10 (긴급 호출)
 * 책임: 스태프가 긴급 상황을 관리자에게 에스컬레이션하는 UI 진입점.
 */
public class SOSAlertView {

    private final SOSController sosController;

    public SOSAlertView(SOSController sosController) {
        this.sosController = sosController;
    }

    /**
     * 스태프가 긴급 호출(SOS)을 발송한다.
     * Actor Action: Staff triggers an SOS emergency alert.
     */
    public SOSAlertDTO triggerSOS(SOSAlertRequestDTO request) {
        return sosController.triggerSOS(request);
    }
}
