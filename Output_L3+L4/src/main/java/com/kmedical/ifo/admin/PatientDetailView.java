package com.kmedical.ifo.admin;

import com.kmedical.control.PatientController;
import com.kmedical.dto.patient.EmergencyContactDTO;
import com.kmedical.dto.patient.MedicalQuestionnaireDTO;
import com.kmedical.dto.patient.PatientDTO;

import java.util.List;

/**
 * IFO-A03 — PatientDetailView
 * UC: UC-A03 (환자 상세 조회)
 * 책임: 관리자가 특정 환자의 상세 정보(의료문진, 비상연락처)를 조회하는 UI 진입점.
 */
public class PatientDetailView {

    private final PatientController patientController;

    public PatientDetailView(PatientController patientController) {
        this.patientController = patientController;
    }

    /**
     * 관리자가 환자 상세 화면을 연다.
     * Actor Action: Admin opens patient detail view.
     */
    public PatientDTO openPatientDetail(String patientId) {
        return patientController.getPatient(patientId);
    }

    /**
     * 관리자가 의료 문진 내용을 조회한다.
     * Actor Action: Admin views the medical questionnaire.
     */
    public MedicalQuestionnaireDTO viewMedicalQuestionnaire(String patientId) {
        return patientController.getMedicalQuestionnaire(patientId);
    }

    /**
     * 관리자가 비상 연락처 목록을 조회한다.
     * Actor Action: Admin views emergency contacts.
     */
    public List<EmergencyContactDTO> viewEmergencyContacts(String patientId) {
        return patientController.getEmergencyContacts(patientId);
    }

    /**
     * 관리자가 환자 온보딩을 승인한다.
     * Actor Action: Admin approves patient onboarding.
     */
    public PatientDTO approveOnboarding(String patientId, String approvedBy) {
        return patientController.approveOnboarding(patientId, approvedBy);
    }
}
