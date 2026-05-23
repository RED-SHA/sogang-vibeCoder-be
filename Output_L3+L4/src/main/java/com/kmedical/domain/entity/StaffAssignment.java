package com.kmedical.domain.entity;

import com.kmedical.domain.enums.StaffRole;

import java.time.LocalDateTime;

/**
 * C16 — StaffAssignment «entity»
 * 제약: 동일 ScheduleItem에 CHAUFFEUR 1명, INTERPRETER 1명만 배정 가능
 */
public class StaffAssignment {

    private String assignmentId;
    private String scheduleItemId;
    private String staffId;
    private StaffRole staffRole;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime notificationSentAt;

    public StaffAssignment() {}

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getScheduleItemId() { return scheduleItemId; }
    public void setScheduleItemId(String scheduleItemId) { this.scheduleItemId = scheduleItemId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public StaffRole getStaffRole() { return staffRole; }
    public void setStaffRole(StaffRole staffRole) { this.staffRole = staffRole; }

    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getNotificationSentAt() { return notificationSentAt; }
    public void setNotificationSentAt(LocalDateTime notificationSentAt) { this.notificationSentAt = notificationSentAt; }
}
