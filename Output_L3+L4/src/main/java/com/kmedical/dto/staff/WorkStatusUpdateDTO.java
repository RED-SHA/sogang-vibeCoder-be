package com.kmedical.dto.staff;

import com.kmedical.domain.enums.WorkStatus;

import java.time.LocalDateTime;

/** Interface → WorkController 간 업무 상태 변경 요청/응답 DTO */
public class WorkStatusUpdateDTO {

    private String assignmentId;
    private String staffId;
    private WorkStatus newStatus;
    private LocalDateTime changedAt;

    public WorkStatusUpdateDTO() {}

    public WorkStatusUpdateDTO(String assignmentId, String staffId, WorkStatus newStatus) {
        this.assignmentId = assignmentId;
        this.staffId = staffId;
        this.newStatus = newStatus;
        this.changedAt = LocalDateTime.now();
    }

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public WorkStatus getNewStatus() { return newStatus; }
    public void setNewStatus(WorkStatus newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
