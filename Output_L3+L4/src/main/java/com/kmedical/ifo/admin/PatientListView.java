package com.kmedical.ifo.admin;

import com.kmedical.control.PatientController;
import com.kmedical.dto.patient.PatientDTO;

import java.util.List;

/**
 * IFO-A02 — PatientListView
 * UC: UC-A03 (환자 목록 조회)
 * 책임: 관리자가 환자 목록을 열람하고 특정 환자를 선택하는 UI 진입점.
 */
public class PatientListView {

    private final PatientController patientController;

    public PatientListView(PatientController patientController) {
        this.patientController = patientController;
    }

    /**
     * 관리자가 환자 목록 화면을 연다.
     * Actor Action: Admin views the list of patients.
     */
    public PatientDTO viewPatient(String patientId) {
        return patientController.getPatient(patientId);
    }
}
