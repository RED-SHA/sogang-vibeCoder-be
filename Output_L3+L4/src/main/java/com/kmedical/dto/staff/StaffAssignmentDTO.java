package com.kmedical.dto.staff;

import com.kmedical.domain.enums.StaffRole;

import java.time.LocalDateTime;

/** StaffAssignment 도메인 복사 DTO — Interface 계층 노출용 */
public class StaffAssignmentDTO {

    private String assignmentId;
    private String scheduleItemId;
    private String staffId;
    private StaffRole staffRole;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime notificationSentAt;

    public StaffAssignmentDTO() {}

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
