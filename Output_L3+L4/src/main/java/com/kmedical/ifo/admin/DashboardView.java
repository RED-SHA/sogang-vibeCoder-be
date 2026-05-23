package com.kmedical.ifo.admin;

import com.kmedical.control.DashboardController;
import com.kmedical.control.SOSController;
import com.kmedical.dto.dashboard.DashboardDataDTO;
import com.kmedical.dto.sos.SOSAlertDTO;

/**
 * IFO-A01 — DashboardView
 * UC: UC-A02 (관리자 대시보드 조회)
 * 책임: 관리자가 대시보드를 조회하고 미해결 SOS를 확인하는 UI 진입점.
 */
public class DashboardView {

    private final DashboardController dashboardController;
    private final SOSController sosController;

    public DashboardView(DashboardController dashboardController, SOSController sosController) {
        this.dashboardController = dashboardController;
        this.sosController = sosController;
    }

    /**
     * 관리자가 대시보드 화면을 연다.
     * Actor Action: Admin opens the dashboard screen.
     */
    public DashboardDataDTO openDashboard(String agencyId, String adminUserId) {
        return dashboardController.getDashboardData(agencyId, adminUserId);
    }

    /**
     * 관리자가 미해결 SOS 목록을 확인한다.
     * Actor Action: Admin views unresolved SOS alerts.
     */
    public java.util.List<SOSAlertDTO> viewUnresolvedSOS() {
        return sosController.getUnresolvedSOS();
    }

    /**
     * 관리자가 SOS를 해결 처리한다.
     * Actor Action: Admin resolves an SOS alert.
     */
    public SOSAlertDTO resolveSOS(String emergencyAlertId, String resolvedBy, String notes) {
        return sosController.resolveSOS(emergencyAlertId, resolvedBy, notes);
    }
}
