package com.kmedical.control;

import com.kmedical.domain.entity.EmergencyContact;
import com.kmedical.domain.entity.MedicalQuestionnaire;
import com.kmedical.domain.entity.Patient;
import com.kmedical.domain.enums.OnboardingStatus;
import com.kmedical.dto.patient.EmergencyContactDTO;
import com.kmedical.dto.patient.MedicalQuestionnaireDTO;
import com.kmedical.dto.patient.PatientDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C04 — PatientController
 * 책임: 환자 온보딩, 문진, 긴급연락처 CRUD (최대 2개 제한).
 * UC: UC-A03, UC-P04, UC-P05
 *
 * NFR 적용: ConcurrentHashMap(Thread-safe), E.164 전화 검증, fullNameEn 검증
 */
public class PatientController {

    private static final int MAX_EMERGENCY_CONTACTS = 2;

    private final Map<String, Patient>               patientStore       = new ConcurrentHashMap<>();
    private final Map<String, MedicalQuestionnaire>  questionnaireStore = new ConcurrentHashMap<>();
    private final Map<String, List<EmergencyContact>> contactStore      = new ConcurrentHashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("PatientController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 환자 정보를 조회한다.
     */
    public PatientDTO getPatient(String patientId) {
        guardNotClosedDown();
        return toDTO(findPatient(patientId));
    }

    /**
     * 환자 온보딩 상태를 SUBMITTED로 변경한다.
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
        if (patient.getOnboardingStatus() != OnboardingStatus.SUBMITTED)
            throw new IllegalStateException("Patient onboarding is not in SUBMITTED state.");
        patient.setOnboardingStatus(OnboardingStatus.APPROVED);
        return toDTO(patient);
    }

    /**
     * 환자 프로필 정보를 갱신한다 (nationality, dateOfBirth, fullNameEn).
     * 검증: nationality → ISO 3166-1 alpha-2, dateOfBirth → 1900-01-01 이후 과거/오늘
     */
    public PatientDTO savePatientProfile(PatientDTO dto) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(dto, "PatientDTO");
        ValidationUtil.requireNotBlank(dto.getUserId(), "userId");

        if (dto.getFullNameEn() != null && !dto.getFullNameEn().isEmpty()) {
            ValidationUtil.requireValidFullNameEn(dto.getFullNameEn(), "fullNameEn");
        }
        if (dto.getNationality() != null && !dto.getNationality().isEmpty()) {
            dto.setNationality(dto.getNationality().toUpperCase());
            ValidationUtil.requireValidIso2Country(dto.getNationality());
        }
        if (dto.getDateOfBirth() != null) {
            ValidationUtil.requirePastOrPresentDate(dto.getDateOfBirth(), "dateOfBirth");
        }

        Patient patient = findPatient(dto.getUserId());
        if (dto.getFullNameEn() != null && !dto.getFullNameEn().isEmpty()) {
            patient.setFullNameEn(dto.getFullNameEn());
        }
        if (dto.getNationality() != null && !dto.getNationality().isEmpty()) {
            patient.setNationality(dto.getNationality());
        }
        if (dto.getDateOfBirth() != null) {
            patient.setDateOfBirth(dto.getDateOfBirth());
        }
        return toDTO(patient);
    }

    /**
     * 영문 의료 문진표를 저장한다.
     * 검증: patientId not null, 목록 항목 길이 제한
     */
    public MedicalQuestionnaireDTO saveMedicalQuestionnaire(MedicalQuestionnaireDTO dto) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(dto, "MedicalQuestionnaireDTO");
        ValidationUtil.requireNotBlank(dto.getPatientId(), "patientId");

        // 각 목록 항목 건수·길이 검증
        if (dto.getCurrentMedications() != null && dto.getCurrentMedications().size() > 50)
            throw new IllegalArgumentException("currentMedications must not exceed 50 items.");
        if (dto.getAllergies() != null && dto.getAllergies().size() > 30)
            throw new IllegalArgumentException("allergies must not exceed 30 items.");
        if (dto.getPastSurgeries() != null && dto.getPastSurgeries().size() > 20)
            throw new IllegalArgumentException("pastSurgeries must not exceed 20 items.");
        validateStringList(dto.getCurrentMedications(), 200, "currentMedications item");
        validateStringList(dto.getAllergies(), 200, "allergies item");
        validateStringList(dto.getPastSurgeries(), 300, "pastSurgeries item");
        ValidationUtil.requireMaxLength(dto.getMedicalNotes(), 3000, "medicalNotes");

        MedicalQuestionnaire q = new MedicalQuestionnaire();
        q.setQuestionnaireId(UUID.randomUUID().toString());
        q.setPatientId(dto.getPatientId());
        q.setCurrentMedications(dto.getCurrentMedications() != null ? dto.getCurrentMedications() : new ArrayList<>());
        q.setAllergies(dto.getAllergies()          != null ? dto.getAllergies()          : new ArrayList<>());
        q.setPastSurgeries(dto.getPastSurgeries()  != null ? dto.getPastSurgeries()     : new ArrayList<>());
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
     * 검증: phoneE164 E.164 형식, fullNameEn 영문만, relationship 필수
     */
    public EmergencyContactDTO addEmergencyContact(EmergencyContactDTO dto) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(dto, "EmergencyContactDTO");
        ValidationUtil.requireNotBlank(dto.getPatientId(), "patientId");
        ValidationUtil.requireValidFullNameEn(dto.getFullNameEn(), "fullNameEn");
        ValidationUtil.requireLengthBetween(dto.getRelationship(), 1, 50, "relationship");
        ValidationUtil.requireValidE164Phone(dto.getPhoneE164());

        List<EmergencyContact> contacts = contactStore.computeIfAbsent(
                dto.getPatientId(), k -> new CopyOnWriteArrayList<>());
        if (contacts.size() >= MAX_EMERGENCY_CONTACTS)
            throw new IllegalStateException("Emergency contact limit exceeded: max 2 per patient.");

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

    /**
     * 신규 환자 등록 (AuthController 위임 처리)
     */
    public void registerPatient(Patient patient) {
        patientStore.put(patient.getUserId(), patient);
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

    private void validateStringList(List<String> list, int maxItemLen, String fieldName) {
        if (list == null) return;
        for (String item : list) {
            ValidationUtil.requireMaxLength(item, maxItemLen, fieldName);
        }
    }
}
