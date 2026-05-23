package com.kmedical.dto.staff;

import java.time.LocalDateTime;

/** Interface → WorkController 간 증빙 사진 업로드 DTO */
public class WorkProofPhotoDTO {

    private String assignmentId;
    private String patientJourneyId;
    private String staffId;
    private String fileUrl;
    private LocalDateTime takenAt;

    public WorkProofPhotoDTO() {}

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getPatientJourneyId() { return patientJourneyId; }
    public void setPatientJourneyId(String patientJourneyId) { this.patientJourneyId = patientJourneyId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }
}
