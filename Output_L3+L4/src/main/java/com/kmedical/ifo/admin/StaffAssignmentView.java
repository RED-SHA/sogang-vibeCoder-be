package com.kmedical.ifo.admin;

import com.kmedical.control.StaffAssignmentController;
import com.kmedical.control.StaffController;
import com.kmedical.dto.staff.StaffAssignmentCreateRequestDTO;
import com.kmedical.dto.staff.StaffAssignmentDTO;
import com.kmedical.dto.staff.StaffDTO;

import java.util.List;

/**
 * IFO-A08 — StaffAssignmentView
 * UC: UC-A07 (스태프 배정)
 * 책임: 관리자가 스케줄 항목에 스태프를 배정하는 UI 진입점.
 */
public class StaffAssignmentView {

    private final StaffAssignmentController staffAssignmentController;
    private final StaffController staffController;

    public StaffAssignmentView(StaffAssignmentController staffAssignmentController,
                                StaffController staffController) {
        this.staffAssignmentController = staffAssignmentController;
        this.staffController = staffController;
    }

    /**
     * 관리자가 에이전시 스태프 목록을 조회한다.
     * Actor Action: Admin views available staff members.
     */
    public List<StaffDTO> viewAvailableStaff(String agencyId) {
        return staffController.getStaffByAgency(agencyId);
    }

    /**
     * 관리자가 스태프를 스케줄 항목에 배정한다.
     * Actor Action: Admin assigns a staff member to a schedule item.
     */
    public StaffAssignmentDTO assignStaff(StaffAssignmentCreateRequestDTO request) {
        return staffAssignmentController.assignStaff(request);
    }

    /**
     * 관리자가 스케줄 항목의 배정 목록을 조회한다.
     * Actor Action: Admin views assignments for a schedule item.
     */
    public List<StaffAssignmentDTO> viewAssignmentsForScheduleItem(String scheduleItemId) {
        return staffAssignmentController.getAssignmentsByScheduleItem(scheduleItemId);
    }
}
