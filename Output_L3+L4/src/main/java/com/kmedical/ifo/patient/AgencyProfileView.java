package com.kmedical.ifo.patient;

import com.kmedical.control.AgencyController;
import com.kmedical.dto.agency.AgencyDTO;

/**
 * IFO-P06 — AgencyProfileView
 * UC: UC-P06 (에이전시 정보 열람)
 * 책임: 환자가 담당 에이전시의 프로필 정보를 조회하는 UI 진입점.
 */
public class AgencyProfileView {

    private final AgencyController agencyController;

    public AgencyProfileView(AgencyController agencyController) {
        this.agencyController = agencyController;
    }

    /**
     * 환자가 에이전시 정보를 조회한다.
     * Actor Action: Patient views the agency profile information.
     */
    public AgencyDTO viewAgencyProfile(String agencyId) {
        return agencyController.getAgency(agencyId);
    }
}
