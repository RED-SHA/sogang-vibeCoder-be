package com.kmedical.ifo.patient;

import com.kmedical.control.PatientController;
import com.kmedical.dto.patient.MedicalQuestionnaireDTO;

/**
 * IFO-P04 — MedicalQuestionnaireForm
 * UC: UC-P04 (의료 문진 작성)
 * 책임: 환자가 의료 문진 폼을 작성하고 제출하는 UI 진입점.
 */
public class MedicalQuestionnaireForm {

    private final PatientController patientController;

    public MedicalQuestionnaireForm(PatientController patientController) {
        this.patientController = patientController;
    }

    /**
     * 환자가 의료 문진을 작성하고 저장한다.
     * Actor Action: Patient submits the medical questionnaire.
     */
    public MedicalQuestionnaireDTO saveMedicalQuestionnaire(MedicalQuestionnaireDTO dto) {
        return patientController.saveMedicalQuestionnaire(dto);
    }

    /**
     * 환자가 기존 의료 문진을 조회한다.
     * Actor Action: Patient views the previously submitted questionnaire.
     */
    public MedicalQuestionnaireDTO viewMedicalQuestionnaire(String patientId) {
        return patientController.getMedicalQuestionnaire(patientId);
    }
}
