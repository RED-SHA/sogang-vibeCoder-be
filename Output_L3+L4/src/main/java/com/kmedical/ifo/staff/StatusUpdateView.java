package com.kmedical.ifo.staff;

import com.kmedical.control.WorkController;
import com.kmedical.dto.staff.WorkStatusUpdateDTO;

import java.util.List;

/**
 * IFO-S05 — StatusUpdateView
 * UC: UC-S05 (업무 상태 업데이트)
 * 책임: 스태프가 현재 업무 상태를 갱신하고 이력을 조회하는 UI 진입점.
 */
public class StatusUpdateView {

    private final WorkController workController;

    public StatusUpdateView(WorkController workController) {
        this.workController = workController;
    }

    /**
     * 스태프가 업무 상태를 업데이트한다.
     * Actor Action: Staff updates the work status for an assignment.
     */
    public WorkStatusUpdateDTO updateStatus(WorkStatusUpdateDTO dto) {
        return workController.updateWorkStatus(dto);
    }

    /**
     * 스태프가 업무 상태 변경 이력을 조회한다.
     * Actor Action: Staff views the work status history for an assignment.
     */
    public List<WorkStatusUpdateDTO> viewStatusHistory(String staffAssignmentId) {
        return workController.getStatusHistory(staffAssignmentId);
    }
}
