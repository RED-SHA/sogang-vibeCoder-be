package com.kmedical.control;

import com.kmedical.domain.entity.EmergencyContact;
import com.kmedical.domain.entity.MedicalQuestionnaire;
import com.kmedical.domain.entity.Patient;
import com.kmedical.domain.enums.OnboardingStatus;
import com.kmedical.dto.patient.EmergencyContactDTO;
import com.kmedical.dto.patient.MedicalQuestionnaireDTO;
import com.kmedical.dto.patient.PatientDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C04 — PatientController
 * 책임: 환자 온보딩, 문진, 긴급연락처 CRUD (최대 2개 제한).
 * UC: UC-A03, UC-P04, UC-P05
 */
public class PatientController {

    private static final int MAX_EMERGENCY_CONTACTS = 2;

    private final Map<String, Patient> patientStore = new HashMap<>();
    private final Map<String, MedicalQuestionnaire> questionnaireStore = new HashMap<>();
    private final Map<String, List<EmergencyContact>> contactStore = new HashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 환자 정보를 조회한다.
     * System Response: patientId 검증 → Patient 조회 → DTO 반환
     */
    public PatientDTO getPatient(String patientId) {
        guardNotClosedDown();
        Patient patient = findPatient(patientId);
        return toDTO(patient);
    }

    /**
     * 환자 온보딩 상태를 SUBMITTED로 변경한다.
     * System Response: 서류 제출 확인 → 상태 전이
     */
    public PatientDTO submitOnboarding(String patientId) {
        guardNotClosedDown();
        Patient patient = findPatient(patientId);
        patient.setOnboardingStatus(OnboardingStatus.SUBMITTED);
        return toDTO(patient);
    }

    /**
     * 관리자가 온보딩을 승인한다.
     */
    public PatientDTO approveOnboarding(String patientId, String adminId) {
        guardNotClosedDown();
        Patient patient = findPatient(patientId);
        if (patient.getOnboardingStatus() != OnboardingStatus.SUBMITTED) {
            throw new IllegalStateException("Patient onboarding is not in SUBMITTED state.");
        }
        patient.setOnboardingStatus(OnboardingStatus.APPROVED);
        return toDTO(patient);
    }

    /**
     * 영문 의료 문진표를 저장한다.
     * System Response: 입력 검증 → MedicalQuestionnaire 생성 및 저장
     */
    public MedicalQuestionnaireDTO saveMedicalQuestionnaire(MedicalQuestionnaireDTO dto) {
        guardNotClosedDown();
        if (dto == null || dto.getPatientId() == null) {
            throw new IllegalArgumentException("MedicalQuestionnaire data is incomplete.");
        }

        MedicalQuestionnaire q = new MedicalQuestionnaire();
        q.setQuestionnaireId(UUID.randomUUID().toString());
        q.setPatientId(dto.getPatientId());
        q.setCurrentMedications(dto.getCurrentMedications());
        q.setAllergies(dto.getAllergies());
        q.setPastSurgeries(dto.getPastSurgeries());
        q.setMedicalNotes(dto.getMedicalNotes());
        q.setSubmittedAt(LocalDateTime.now());

        questionnaireStore.put(q.getPatientId(), q);
        return toQuestionnaireDTO(q);
    }

    /**
     * 환자의 문진표를 조회한다.
     */
    public MedicalQuestionnaireDTO getMedicalQuestionnaire(String patientId) {
        guardNotClosedDown();
        MedicalQuestionnaire q = questionnaireStore.get(patientId);
        if (q == null) throw new IllegalArgumentException("MedicalQuestionnaire not found for patient: " + patientId);
        return toQuestionnaireDTO(q);
    }

    /**
     * 긴급 연락처를 등록한다 (최대 2건 제한).
     * System Response: 건수 제한 확인 → EmergencyContact 저장
     */
    public EmergencyContactDTO addEmergencyContact(EmergencyContactDTO dto) {
        guardNotClosedDown();
        if (dto == null || dto.getPatientId() == null) {
            throw new IllegalArgumentException("EmergencyContact data is incomplete.");
        }

        List<EmergencyContact> contacts = contactStore.computeIfAbsent(dto.getPatientId(), k -> new ArrayList<>());
        if (contacts.size() >= MAX_EMERGENCY_CONTACTS) {
            throw new IllegalStateException("Emergency contacts limit reached (max " + MAX_EMERGENCY_CONTACTS + ").");
        }

        EmergencyContact contact = new EmergencyContact();
        contact.setContactId(UUID.randomUUID().toString());
        contact.setPatientId(dto.getPatientId());
        contact.setFullNameEn(dto.getFullNameEn());
        contact.setRelationship(dto.getRelationship());
        contact.setPhoneE164(dto.getPhoneE164());
        contact.setSortOrder(contacts.size() + 1);
        contacts.add(contact);

        return toContactDTO(contact);
    }

    /**
     * 긴급 연락처 목록을 조회한다.
     */
    public List<EmergencyContactDTO> getEmergencyContacts(String patientId) {
        guardNotClosedDown();
        List<EmergencyContact> contacts = contactStore.getOrDefault(patientId, new ArrayList<>());
        List<EmergencyContactDTO> result = new ArrayList<>();
        for (EmergencyContact c : contacts) result.add(toContactDTO(c));
        return result;
    }

    /**
     * 긴급 연락처를 삭제한다.
     */
    public void removeEmergencyContact(String patientId, String contactId) {
        guardNotClosedDown();
        List<EmergencyContact> contacts = contactStore.getOrDefault(patientId, new ArrayList<>());
        contacts.removeIf(c -> c.getContactId().equals(contactId));
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private Patient findPatient(String patientId) {
        Patient p = patientStore.get(patientId);
        if (p == null) throw new IllegalArgumentException("Patient not found: " + patientId);
        return p;
    }

    private PatientDTO toDTO(Patient p) {
        PatientDTO dto = new PatientDTO();
        dto.setUserId(p.getUserId());
        dto.setEmail(p.getEmail());
        dto.setFullNameEn(p.getFullNameEn());
        dto.setDateOfBirth(p.getDateOfBirth());
        dto.setNationality(p.getNationality());
        dto.setOnboardingStatus(p.getOnboardingStatus());
        dto.setPreferredLanguage(p.getPreferredLanguage());
        return dto;
    }

    private MedicalQuestionnaireDTO toQuestionnaireDTO(MedicalQuestionnaire q) {
        MedicalQuestionnaireDTO dto = new MedicalQuestionnaireDTO();
        dto.setQuestionnaireId(q.getQuestionnaireId());
        dto.setPatientId(q.getPatientId());
        dto.setCurrentMedications(q.getCurrentMedications());
        dto.setAllergies(q.getAllergies());
        dto.setPastSurgeries(q.getPastSurgeries());
        dto.setMedicalNotes(q.getMedicalNotes());
        dto.setSubmittedAt(q.getSubmittedAt());
        return dto;
    }

    private EmergencyContactDTO toContactDTO(EmergencyContact c) {
        EmergencyContactDTO dto = new EmergencyContactDTO();
        dto.setContactId(c.getContactId());
        dto.setPatientId(c.getPatientId());
        dto.setFullNameEn(c.getFullNameEn());
        dto.setRelationship(c.getRelationship());
        dto.setPhoneE164(c.getPhoneE164());
        dto.setSortOrder(c.getSortOrder());
        return dto;
    }

    public void registerPatient(Patient patient) {
        patientStore.put(patient.getUserId(), patient);
    }
}
