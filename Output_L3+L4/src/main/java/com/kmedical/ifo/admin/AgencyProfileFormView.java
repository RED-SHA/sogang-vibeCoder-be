package com.kmedical.ifo.admin;

import com.kmedical.control.AgencyController;
import com.kmedical.dto.agency.AgencyDTO;

/**
 * IFO-A13 — AgencyProfileFormView
 * UC: UC-A12 (에이전시 프로필 관리)
 * 책임: 관리자가 에이전시 프로필을 조회·수정하는 UI 진입점.
 */
public class AgencyProfileFormView {

    private final AgencyController agencyController;

    public AgencyProfileFormView(AgencyController agencyController) {
        this.agencyController = agencyController;
    }

    /**
     * 관리자가 에이전시 프로필을 조회한다.
     * Actor Action: Admin views agency profile.
     */
    public AgencyDTO viewAgencyProfile(String agencyId) {
        return agencyController.getAgency(agencyId);
    }

    /**
     * 관리자가 에이전시 프로필을 수정·저장한다.
     * Actor Action: Admin saves updated agency profile.
     */
    public AgencyDTO saveAgencyProfile(AgencyDTO dto) {
        return agencyController.saveAgency(dto);
    }
}
