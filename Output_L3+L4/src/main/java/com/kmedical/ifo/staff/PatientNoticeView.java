package com.kmedical.ifo.staff;

import com.kmedical.control.JourneyController;
import com.kmedical.control.PatientController;
import com.kmedical.dto.journey.ScheduleItemDTO;
import com.kmedical.dto.patient.PatientDTO;

import java.util.List;

/**
 * IFO-S04 — PatientNoticeView
 * UC: UC-S04 (환자 공지 사항 확인)
 * 책임: 스태프가 담당 환자의 기본 정보와 당일 일정을 조회하는 UI 진입점.
 */
public class PatientNoticeView {

    private final PatientController patientController;
    private final JourneyController journeyController;

    public PatientNoticeView(PatientController patientController, JourneyController journeyController) {
        this.patientController = patientController;
        this.journeyController = journeyController;
    }

    /**
     * 스태프가 환자 정보를 조회한다.
     * Actor Action: Staff views the patient's basic information.
     */
    public PatientDTO viewPatientInfo(String patientId) {
        return patientController.getPatient(patientId);
    }

    /**
     * 스태프가 해당 여정의 일정 항목 목록을 조회한다.
     * Actor Action: Staff views the schedule items for the patient's journey.
     */
    public List<ScheduleItemDTO> viewJourneySchedule(String journeyId) {
        return journeyController.getScheduleItems(journeyId);
    }
}
