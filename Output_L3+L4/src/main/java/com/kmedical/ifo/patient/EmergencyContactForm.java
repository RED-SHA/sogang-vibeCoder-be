package com.kmedical.ifo.patient;

import com.kmedical.control.PatientController;
import com.kmedical.dto.patient.EmergencyContactDTO;

import java.util.List;

/**
 * IFO-P05 — EmergencyContactForm
 * UC: UC-P05 (비상 연락처 등록)
 * 책임: 환자가 비상 연락처를 추가·조회·삭제하는 UI 진입점.
 *       최대 2명 제한은 PatientController에서 강제된다.
 */
public class EmergencyContactForm {

    private final PatientController patientController;

    public EmergencyContactForm(PatientController patientController) {
        this.patientController = patientController;
    }

    /**
     * 환자가 비상 연락처를 추가한다.
     * Actor Action: Patient adds an emergency contact.
     */
    public EmergencyContactDTO addEmergencyContact(EmergencyContactDTO dto) {
        return patientController.addEmergencyContact(dto);
    }

    /**
     * 환자가 비상 연락처 목록을 조회한다.
     * Actor Action: Patient views the list of emergency contacts.
     */
    public List<EmergencyContactDTO> viewEmergencyContacts(String patientId) {
        return patientController.getEmergencyContacts(patientId);
    }

    /**
     * 환자가 비상 연락처를 삭제한다.
     * Actor Action: Patient removes an emergency contact.
     */
    public void removeEmergencyContact(String patientId, String contactId) {
        patientController.removeEmergencyContact(patientId, contactId);
    }
}
