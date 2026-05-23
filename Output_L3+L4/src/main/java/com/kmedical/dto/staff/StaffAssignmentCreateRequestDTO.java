package com.kmedical.dto.staff;

import com.kmedical.domain.enums.StaffRole;

/** Interface → StaffAssignmentController 간 실무자 배정 요청 DTO */
public class StaffAssignmentCreateRequestDTO {

    private String scheduleItemId;
    private String staffId;
    private StaffRole staffRole;
    private String assignedBy;

    public StaffAssignmentCreateRequestDTO() {}

    public String getScheduleItemId() { return scheduleItemId; }
    public void setScheduleItemId(String scheduleItemId) { this.scheduleItemId = scheduleItemId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public StaffRole getStaffRole() { return staffRole; }
    public void setStaffRole(StaffRole staffRole) { this.staffRole = staffRole; }

    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
}
