package com.kmedical.ifo.admin;

import com.kmedical.control.JourneyController;
import com.kmedical.dto.journey.PatientJourneyDTO;
import com.kmedical.dto.journey.ScheduleItemDTO;
import com.kmedical.dto.journey.ScheduleItemUpdateRequestDTO;

import java.util.List;

/**
 * IFO-A07 — ScheduleEditorView
 * UC: UC-A06 (일정 편집)
 * 책임: 관리자가 환자 여정의 스케줄 항목을 조회·추가·수정하는 UI 진입점.
 */
public class ScheduleEditorView {

    private final JourneyController journeyController;

    public ScheduleEditorView(JourneyController journeyController) {
        this.journeyController = journeyController;
    }

    /**
     * 관리자가 여정 정보를 조회한다.
     * Actor Action: Admin views the patient journey detail.
     */
    public PatientJourneyDTO viewJourney(String journeyId) {
        return journeyController.getJourney(journeyId);
    }

    /**
     * 관리자가 스케줄 항목 목록을 조회한다.
     * Actor Action: Admin views schedule items for a journey.
     */
    public List<ScheduleItemDTO> viewScheduleItems(String journeyId) {
        return journeyController.getScheduleItems(journeyId);
    }

    /**
     * 관리자가 스케줄 항목을 추가한다.
     * Actor Action: Admin adds a schedule item to the journey.
     */
    public ScheduleItemDTO addScheduleItem(ScheduleItemDTO dto) {
        return journeyController.addScheduleItem(dto);
    }

    /**
     * 관리자가 스케줄 항목 상태를 변경한다.
     * Actor Action: Admin updates a schedule item status.
     */
    public ScheduleItemDTO updateScheduleItem(ScheduleItemUpdateRequestDTO request) {
        return journeyController.updateScheduleItem(request);
    }
}
