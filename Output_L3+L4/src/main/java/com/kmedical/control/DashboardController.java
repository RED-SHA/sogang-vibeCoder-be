package com.kmedical.control;

import com.kmedical.dto.alert.AlertDTO;
import com.kmedical.dto.dashboard.DashboardDataDTO;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.quotation.QuotationRequestDTO;
import com.kmedical.dto.sos.SOSAlertDTO;
import com.kmedical.dto.staff.StaffDTO;

import java.util.List;

/**
 * SRV-C19 — DashboardController
 * 책임: 관리자 대시보드용 집계 데이터 제공.
 * UC: UC-A02
 * 조율 Entity: C14 PatientJourney, C04 Staff, C27 Alert, C11 QuotationRequest, C28 EmergencyAlert
 */
public class DashboardController {

    private final JourneyController journeyController;
    private final StaffController staffController;
    private final AlertController alertController;
    private final QuotationController quotationController;
    private final SOSController sosController;

    public DashboardController(JourneyController journeyController,
                                StaffController staffController,
                                AlertController alertController,
                                QuotationController quotationController,
                                SOSController sosController) {
        this.journeyController = journeyController;
        this.staffController = staffController;
        this.alertController = alertController;
        this.quotationController = quotationController;
        this.sosController = sosController;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 관리자 대시보드 데이터를 집계하여 반환한다.
     * System Response: 진행 중 여정·스태프 현황·미해결 SOS·알림·견적 요청 집계
     * 조율 대상: C14 PatientJourney, C04 Staff, C27 Alert, C11 QuotationRequest, C28 EmergencyAlert
     */
    public DashboardDataDTO getDashboardData(String agencyId, String adminUserId) {
        guardNotClosedDown();
        if (agencyId == null) {
            throw new IllegalArgumentException("AgencyId is required for dashboard.");
        }

        DashboardDataDTO data = new DashboardDataDTO();

        // C28 EmergencyAlert — 미해결 SOS
        List<SOSAlertDTO> unresolvedSOS = sosController.getUnresolvedSOS();
        data.setUnresolvedSOS(unresolvedSOS);

        // C04 Staff — 에이전시 소속 전체 스태프
        List<StaffDTO> staffList = staffController.getStaffByAgency(agencyId);
        data.setAvailableStaff(staffList);
        data.setTotalActiveStaff(staffList.size());

        // C27 Alert — 관리자 수신 최근 알림
        if (adminUserId != null) {
            List<AlertDTO> recentAlerts = alertController.getAlertsForUser(adminUserId);
            data.setRecentAlerts(recentAlerts);
        }

        // C11 QuotationRequest — OPEN 상태 견적 요청 목록
        List<QuotationRequestDTO> openRequests = quotationController.getOpenRequests(agencyId);
        data.setOpenRequests(openRequests);

        // C14 PatientJourney — 진행 중 여정 목록
        List<PatientJourneyDTO> activeJourneys = journeyController.getActiveJourneysByAgency(agencyId);
        data.setActiveJourneys(activeJourneys);
        data.setTotalActivePatients(activeJourneys.size());

        return data;
    }
}
