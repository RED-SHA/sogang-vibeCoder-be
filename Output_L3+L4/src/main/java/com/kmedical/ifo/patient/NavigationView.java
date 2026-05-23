package com.kmedical.ifo.patient;

import com.kmedical.control.JourneyController;
import com.kmedical.dto.journey.ScheduleItemDTO;

/**
 * IFO-P11 — NavigationView (Patient)
 * UC: UC-P11 (내비게이션)
 * 책임: 환자가 현재 일정 항목의 목적지 정보를 조회하는 UI 진입점.
 *       실제 지도/경로 안내(NavigationService E09)는 클라이언트 측에서 직접 호출한다.
 */
public class NavigationView {

    private final JourneyController journeyController;

    public NavigationView(JourneyController journeyController) {
        this.journeyController = journeyController;
    }

    /**
     * 환자가 현재 이동 대상 일정 항목을 조회한다.
     * Actor Action: Patient views the current schedule item for navigation.
     */
    public ScheduleItemDTO viewCurrentScheduleForNavigation(String journeyId) {
        return journeyController.getScheduleItemForNavigation(journeyId);
    }
}
