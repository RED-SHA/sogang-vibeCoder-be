package com.kmedical.ifo.patient;

import com.kmedical.control.JourneyController;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.journey.ScheduleItemDTO;

import java.util.List;

/**
 * IFO-P10 — ItineraryView
 * UC: UC-P10 (일정 열람)
 * 책임: 환자가 자신의 여정 일정을 조회하는 UI 진입점.
 */
public class ItineraryView {

    private final JourneyController journeyController;

    public ItineraryView(JourneyController journeyController) {
        this.journeyController = journeyController;
    }

    /**
     * 환자가 여정 개요를 조회한다.
     * Actor Action: Patient views the overall journey information.
     */
    public PatientJourneyDTO viewJourney(String journeyId) {
        return journeyController.getJourney(journeyId);
    }

    /**
     * 환자가 일정 항목 목록을 조회한다.
     * Actor Action: Patient views the schedule items of the journey.
     */
    public List<ScheduleItemDTO> viewScheduleItems(String journeyId) {
        return journeyController.getScheduleItems(journeyId);
    }
}
