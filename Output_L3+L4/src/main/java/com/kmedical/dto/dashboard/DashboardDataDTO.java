package com.kmedical.dto.dashboard;

import java.util.List;

import com.kmedical.dto.alert.AlertDTO;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.quotation.QuotationRequestDTO;
import com.kmedical.dto.sos.SOSAlertDTO;
import com.kmedical.dto.staff.StaffDTO;

/** DashboardController → DashboardView 간 대시보드 집계 데이터 DTO */
public class DashboardDataDTO {

    private List<PatientJourneyDTO> activeJourneys;
    private List<StaffDTO> availableStaff;
    private List<AlertDTO> recentAlerts;
    private List<QuotationRequestDTO> openRequests;
    private List<SOSAlertDTO> unresolvedSOS;
    private int totalActivePatients;
    private int totalActiveStaff;

    public DashboardDataDTO() {}

    public List<PatientJourneyDTO> getActiveJourneys() { return activeJourneys; }
    public void setActiveJourneys(List<PatientJourneyDTO> activeJourneys) { this.activeJourneys = activeJourneys; }

    public List<StaffDTO> getAvailableStaff() { return availableStaff; }
    public void setAvailableStaff(List<StaffDTO> availableStaff) { this.availableStaff = availableStaff; }

    public List<AlertDTO> getRecentAlerts() { return recentAlerts; }
    public void setRecentAlerts(List<AlertDTO> recentAlerts) { this.recentAlerts = recentAlerts; }

    public List<QuotationRequestDTO> getOpenRequests() { return openRequests; }
    public void setOpenRequests(List<QuotationRequestDTO> openRequests) { this.openRequests = openRequests; }

    public List<SOSAlertDTO> getUnresolvedSOS() { return unresolvedSOS; }
    public void setUnresolvedSOS(List<SOSAlertDTO> unresolvedSOS) { this.unresolvedSOS = unresolvedSOS; }

    public int getTotalActivePatients() { return totalActivePatients; }
    public void setTotalActivePatients(int totalActivePatients) { this.totalActivePatients = totalActivePatients; }

    public int getTotalActiveStaff() { return totalActiveStaff; }
    public void setTotalActiveStaff(int totalActiveStaff) { this.totalActiveStaff = totalActiveStaff; }
}
