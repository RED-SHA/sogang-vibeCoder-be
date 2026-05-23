package com.kmedical.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** C28 — EmergencyAlert «entity» */
public class EmergencyAlert {

    private String emergencyAlertId;
    private String staffId;
    private String patientJourneyId;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private String notes;

    public EmergencyAlert() {}

    public String getEmergencyAlertId() { return emergencyAlertId; }
    public void setEmergencyAlertId(String emergencyAlertId) { this.emergencyAlertId = emergencyAlertId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getPatientJourneyId() { return patientJourneyId; }
    public void setPatientJourneyId(String patientJourneyId) { this.patientJourneyId = patientJourneyId; }

    public BigDecimal getLocationLat() { return locationLat; }
    public void setLocationLat(BigDecimal locationLat) { this.locationLat = locationLat; }

    public BigDecimal getLocationLng() { return locationLng; }
    public void setLocationLng(BigDecimal locationLng) { this.locationLng = locationLng; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
