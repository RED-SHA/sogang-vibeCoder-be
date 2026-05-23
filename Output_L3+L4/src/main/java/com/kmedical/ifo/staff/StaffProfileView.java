package com.kmedical.ifo.staff;

import com.kmedical.control.StaffController;
import com.kmedical.domain.enums.StaffAvailability;
import com.kmedical.dto.staff.StaffDTO;

/**
 * IFO-S02 — StaffProfileView
 * UC: UC-S02 (스태프 프로필 관리)
 * 책임: 스태프가 자신의 프로필을 조회·수정하고 가용성을 설정하는 UI 진입점.
 */
public class StaffProfileView {

    private final StaffController staffController;

    public StaffProfileView(StaffController staffController) {
        this.staffController = staffController;
    }

    /**
     * 스태프가 자신의 프로필을 조회한다.
     * Actor Action: Staff views their own profile.
     */
    public StaffDTO viewProfile(String staffId) {
        return staffController.getStaff(staffId);
    }

    /**
     * 스태프가 프로필 정보를 수정한다.
     * Actor Action: Staff updates their profile information.
     */
    public StaffDTO updateProfile(StaffDTO dto) {
        return staffController.updateProfile(dto);
    }

    /**
     * 스태프가 가용성 상태를 변경한다.
     * Actor Action: Staff updates their availability status.
     */
    public StaffDTO updateAvailability(String staffId, StaffAvailability availability) {
        return staffController.updateAvailability(staffId, availability);
    }
}
