package com.kmedical.control;

import com.kmedical.dto.alert.AlertDTO;
import com.kmedical.dto.dashboard.DashboardDataDTO;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.quotation.QuotationRequestDTO;
import com.kmedical.dto.sos.SOSAlertDTO;
import com.kmedical.dto.staff.StaffDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * SRV-C19 — DashboardController
 * 책임: 관리자 대시보드용 집계 데이터 제공.
 * UC: UC-A02
 * NFR-PERF-02: getDashboardData 2000ms 임계값 측정, 부분 데이터 안전 반환
 */
public class DashboardController {

    private static final long DASHBOARD_PERF_THRESHOLD_MS = 2000L;

    private final JourneyController   journeyController;
    private final StaffController     staffController;
    private final AlertController     alertController;
    private final QuotationController quotationController;
    private final SOSController       sosController;

    public DashboardController(JourneyController journeyController,
                                StaffController staffController,
                                AlertController alertController,
                                QuotationController quotationController,
                                SOSController sosController) {
        this.journeyController   = journeyController;
        this.staffController     = staffController;
        this.alertController     = alertController;
        this.quotationController = quotationController;
        this.sosController       = sosController;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("DashboardController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 관리자 대시보드 데이터를 집계하여 반환한다.
     * NFR-PERF-02: 2000ms 초과 시 [WARN] 로그. 개별 조회 실패 시 빈 값으로 폴백.
     * 예외를 클라이언트에게 노출하지 않는다.
     */
    public DashboardDataDTO getDashboardData(String agencyId, String adminUserId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(agencyId, "agencyId");

        long start = System.currentTimeMillis();
        DashboardDataDTO data = new DashboardDataDTO();

        // 미해결 SOS (폴백: 빈 목록)
        try {
            List<SOSAlertDTO> unresolvedSOS = sosController.getUnresolvedSOS();
            data.setUnresolvedSOS(unresolvedSOS);
        } catch (Exception e) {
            AuditLogger.warn("DASHBOARD_SOS_LOAD_FAIL", e.getMessage());
            data.setUnresolvedSOS(new ArrayList<>());
        }

        // 스태프 목록 (폴백: 빈 목록)
        try {
            List<StaffDTO> staffList = staffController.getStaffByAgency(agencyId);
            data.setAvailableStaff(staffList);
            data.setTotalActiveStaff(staffList.size());
        } catch (Exception e) {
            AuditLogger.warn("DASHBOARD_STAFF_LOAD_FAIL", e.getMessage());
            data.setAvailableStaff(new ArrayList<>());
            data.setTotalActiveStaff(0);
        }

        // 최근 알림 (폴백: 빈 목록)
        if (adminUserId != null) {
            try {
                List<AlertDTO> recentAlerts = alertController.getAlertsForUser(adminUserId);
                data.setRecentAlerts(recentAlerts);
            } catch (Exception e) {
                AuditLogger.warn("DASHBOARD_ALERT_LOAD_FAIL", e.getMessage());
                data.setRecentAlerts(new ArrayList<>());
            }
        }

        // OPEN 견적 요청 (폴백: 빈 목록)
        try {
            List<QuotationRequestDTO> openRequests = quotationController.getOpenRequests(agencyId);
            data.setOpenRequests(openRequests);
        } catch (Exception e) {
            AuditLogger.warn("DASHBOARD_QUOTATION_LOAD_FAIL", e.getMessage());
            data.setOpenRequests(new ArrayList<>());
        }

        // 진행 중 여정 (폴백: 빈 목록)
        try {
            List<PatientJourneyDTO> activeJourneys = journeyController.getActiveJourneysByAgency(agencyId);
            data.setActiveJourneys(activeJourneys);
            data.setTotalActivePatients(activeJourneys.size());
        } catch (Exception e) {
            AuditLogger.warn("DASHBOARD_JOURNEY_LOAD_FAIL", e.getMessage());
            data.setActiveJourneys(new ArrayList<>());
            data.setTotalActivePatients(0);
        }

        AuditLogger.perf("DASHBOARD_LOAD", System.currentTimeMillis() - start, DASHBOARD_PERF_THRESHOLD_MS);
        return data;
    }
}
