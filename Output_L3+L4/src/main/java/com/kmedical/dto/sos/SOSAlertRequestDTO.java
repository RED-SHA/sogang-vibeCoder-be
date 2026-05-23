package com.kmedical.dto.sos;

import java.math.BigDecimal;

/** Interface → SOSController 간 긴급 호출 요청 DTO */
public class SOSAlertRequestDTO {

    private String staffId;
    private String patientJourneyId;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private String notes;

    public SOSAlertRequestDTO() {}

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getPatientJourneyId() { return patientJourneyId; }
    public void setPatientJourneyId(String patientJourneyId) { this.patientJourneyId = patientJourneyId; }

    public BigDecimal getLocationLat() { return locationLat; }
    public void setLocationLat(BigDecimal locationLat) { this.locationLat = locationLat; }

    public BigDecimal getLocationLng() { return locationLng; }
    public void setLocationLng(BigDecimal locationLng) { this.locationLng = locationLng; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
