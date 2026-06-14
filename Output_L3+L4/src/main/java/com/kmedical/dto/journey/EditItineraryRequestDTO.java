package com.kmedical.dto.journey;

import com.kmedical.dto.staff.StaffAssignmentCreateRequestDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** UC-ADM-07 aggregate request DTO. */
public class EditItineraryRequestDTO {

    private String operatorId;
    private String patientJourneyId;
    private String scheduleItemId;
    private Integer expectedVersion;
    private ScheduleItemUpdateRequestDTO update;
    private List<StaffAssignmentCreateRequestDTO> staffAssignments;
    private boolean cancelRequested;

    public EditItineraryRequestDTO() {}

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public String getPatientJourneyId() { return patientJourneyId; }
    public void setPatientJourneyId(String patientJourneyId) { this.patientJourneyId = patientJourneyId; }

    public String getScheduleItemId() { return scheduleItemId; }
    public void setScheduleItemId(String scheduleItemId) { this.scheduleItemId = scheduleItemId; }

    public Integer getExpectedVersion() { return expectedVersion; }
    public void setExpectedVersion(Integer expectedVersion) { this.expectedVersion = expectedVersion; }

    public ScheduleItemUpdateRequestDTO getUpdate() { return update; }
    public void setUpdate(ScheduleItemUpdateRequestDTO update) { this.update = update; }

    public List<StaffAssignmentCreateRequestDTO> getStaffAssignments() {
        if (staffAssignments == null) return null;
        return Collections.unmodifiableList(staffAssignments);
    }

    public void setStaffAssignments(List<StaffAssignmentCreateRequestDTO> staffAssignments) {
        this.staffAssignments = staffAssignments == null ? null : new ArrayList<>(staffAssignments);
    }

    public boolean isCancelRequested() { return cancelRequested; }
    public void setCancelRequested(boolean cancelRequested) { this.cancelRequested = cancelRequested; }
}
