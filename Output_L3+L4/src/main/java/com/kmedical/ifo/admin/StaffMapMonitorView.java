package com.kmedical.ifo.admin;

import com.kmedical.control.StaffAssignmentController;
import com.kmedical.control.StaffController;
import com.kmedical.dto.staff.StaffAssignmentDTO;
import com.kmedical.dto.staff.StaffDTO;

import java.util.List;

/**
 * IFO-A09 — StaffMapMonitorView
 * UC: UC-A08 (스태프 현황 모니터링)
 * 책임: 관리자가 스태프의 현재 배정 상태와 가용성을 지도 형태로 모니터링하는 UI 진입점.
 */
public class StaffMapMonitorView {

    private final StaffController staffController;
    private final StaffAssignmentController staffAssignmentController;

    public StaffMapMonitorView(StaffController staffController,
                                StaffAssignmentController staffAssignmentController) {
        this.staffController = staffController;
        this.staffAssignmentController = staffAssignmentController;
    }

    /**
     * 관리자가 에이전시 소속 전체 스태프를 조회한다.
     * Actor Action: Admin monitors all staff in the agency.
     */
    public List<StaffDTO> monitorAllStaff(String agencyId) {
        return staffController.getStaffByAgency(agencyId);
    }

    /**
     * 관리자가 특정 스태프의 배정 현황을 조회한다.
     * Actor Action: Admin views current assignments of a staff member.
     */
    public List<StaffAssignmentDTO> viewStaffAssignments(String staffId) {
        return staffAssignmentController.getAssignmentsByStaff(staffId);
    }
}
