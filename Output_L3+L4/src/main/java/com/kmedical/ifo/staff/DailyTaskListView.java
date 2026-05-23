package com.kmedical.ifo.staff;

import com.kmedical.control.StaffAssignmentController;
import com.kmedical.dto.staff.StaffAssignmentDTO;

import java.util.List;

/**
 * IFO-S03 — DailyTaskListView
 * UC: UC-S03 (일일 업무 목록 조회)
 * 책임: 스태프가 자신에게 배정된 업무 목록을 조회하는 UI 진입점.
 */
public class DailyTaskListView {

    private final StaffAssignmentController staffAssignmentController;

    public DailyTaskListView(StaffAssignmentController staffAssignmentController) {
        this.staffAssignmentController = staffAssignmentController;
    }

    /**
     * 스태프가 자신에게 배정된 업무 목록을 조회한다.
     * Actor Action: Staff views their assigned tasks for today.
     */
    public List<StaffAssignmentDTO> viewMyAssignments(String staffId) {
        return staffAssignmentController.getAssignmentsByStaff(staffId);
    }
}
