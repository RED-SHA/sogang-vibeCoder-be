package com.kmedical.domain.entity;

import com.kmedical.domain.enums.WorkStatus;

import java.time.LocalDateTime;

/** C17 — WorkStatusUpdate «entity» */
public class WorkStatusUpdate {

    private String workStatusUpdateId;
    private String assignmentId;
    private String staffId;
    private WorkStatus newStatus;
    private LocalDateTime changedAt;
    private LocalDateTime syncedAt;

    public WorkStatusUpdate() {}

    public String getWorkStatusUpdateId() { return workStatusUpdateId; }
    public void setWorkStatusUpdateId(String workStatusUpdateId) { this.workStatusUpdateId = workStatusUpdateId; }

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public WorkStatus getNewStatus() { return newStatus; }
    public void setNewStatus(WorkStatus newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
}
