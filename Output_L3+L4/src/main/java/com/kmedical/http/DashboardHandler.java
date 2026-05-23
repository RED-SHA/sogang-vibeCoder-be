package com.kmedical.http;

import com.kmedical.control.DashboardController;
import com.kmedical.dto.alert.AlertDTO;
import com.kmedical.dto.dashboard.DashboardDataDTO;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.quotation.QuotationRequestDTO;
import com.kmedical.dto.sos.SOSAlertDTO;
import com.kmedical.dto.staff.StaffDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * SRV-C19 DashboardController HTTP 매핑
 *
 * GET /api/dashboard?agencyId=&adminUserId=  관리자 대시보드 집계 데이터
 */
public class DashboardHandler extends BaseHandler {

    private final DashboardController dashboardController;

    public DashboardHandler(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        if ("GET".equals(method) && "/api/dashboard".equals(path)) {
            handleGet(ex);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleGet(HttpExchange ex) throws IOException {
        String agencyId     = queryParam(ex, "agencyId");
        String adminUserId  = queryParam(ex, "adminUserId");
        if (agencyId == null) { sendError(ex, 400, "Query parameter 'agencyId' is required."); return; }
        DashboardDataDTO data = dashboardController.getDashboardData(agencyId, adminUserId);
        sendJson(ex, 200, dashboardToMap(data));
    }

    private Map<String, Object> dashboardToMap(DashboardDataDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalActivePatients", dto.getTotalActivePatients());
        m.put("totalActiveStaff",    dto.getTotalActiveStaff());

        List<Map<String, Object>> journeys = new ArrayList<>();
        if (dto.getActiveJourneys() != null) {
            for (PatientJourneyDTO j : dto.getActiveJourneys()) {
                Map<String, Object> jm = new LinkedHashMap<>();
                jm.put("patientJourneyId", j.getPatientJourneyId());
                jm.put("patientId",        j.getPatientId());
                jm.put("status",           j.getStatus() != null ? j.getStatus().name() : null);
                journeys.add(jm);
            }
        }
        m.put("activeJourneys", journeys);

        List<Map<String, Object>> staff = new ArrayList<>();
        if (dto.getAvailableStaff() != null) {
            for (StaffDTO s : dto.getAvailableStaff()) {
                Map<String, Object> sm = new LinkedHashMap<>();
                sm.put("userId",             s.getUserId());
                sm.put("displayNameEn",      s.getDisplayNameEn());
                sm.put("availabilityStatus", s.getAvailabilityStatus() != null ? s.getAvailabilityStatus().name() : null);
                staff.add(sm);
            }
        }
        m.put("availableStaff", staff);

        List<Map<String, Object>> sos = new ArrayList<>();
        if (dto.getUnresolvedSOS() != null) {
            for (SOSAlertDTO s : dto.getUnresolvedSOS()) {
                Map<String, Object> sm = new LinkedHashMap<>();
                sm.put("emergencyAlertId", s.getEmergencyAlertId());
                sm.put("staffId",          s.getStaffId());
                sm.put("reportedAt",       s.getReportedAt() != null ? s.getReportedAt().toString() : null);
                sos.add(sm);
            }
        }
        m.put("unresolvedSOS", sos);

        List<Map<String, Object>> alerts = new ArrayList<>();
        if (dto.getRecentAlerts() != null) {
            for (AlertDTO a : dto.getRecentAlerts()) {
                Map<String, Object> am = new LinkedHashMap<>();
                am.put("alertId",   a.getAlertId());
                am.put("alertType", a.getAlertType() != null ? a.getAlertType().name() : null);
                am.put("content",   a.getContent());
                am.put("sentAt",    a.getSentAt() != null ? a.getSentAt().toString() : null);
                alerts.add(am);
            }
        }
        m.put("recentAlerts", alerts);

        List<Map<String, Object>> requests = new ArrayList<>();
        if (dto.getOpenRequests() != null) {
            for (QuotationRequestDTO r : dto.getOpenRequests()) {
                Map<String, Object> rm = new LinkedHashMap<>();
                rm.put("quotationRequestId", r.getQuotationRequestId());
                rm.put("patientId",          r.getPatientId());
                rm.put("status",             r.getStatus() != null ? r.getStatus().name() : null);
                requests.add(rm);
            }
        }
        m.put("openRequests", requests);

        return m;
    }
}
