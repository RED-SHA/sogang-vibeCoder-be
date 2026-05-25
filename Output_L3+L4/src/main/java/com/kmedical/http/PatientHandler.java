package com.kmedical.http;

import com.kmedical.control.PatientController;
import com.kmedical.dto.patient.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * SRV-C04 PatientController HTTP 매핑
 *
 * GET    /api/patients/{id}                          환자 조회
 * PUT    /api/patients/{id}/profile                  환자 프로필 갱신 (ISO2 국적, 생년월일)
 * POST   /api/patients/{id}/onboarding/submit        UC-P04 온보딩 제출
 * POST   /api/patients/{id}/onboarding/approve       관리자 온보딩 승인
 * POST   /api/patients/{id}/questionnaire            문진표 저장
 * GET    /api/patients/{id}/questionnaire            문진표 조회
 * POST   /api/patients/{id}/emergency-contacts       UC-P05 긴급연락처 추가
 * GET    /api/patients/{id}/emergency-contacts       긴급연락처 목록
 * DELETE /api/patients/{id}/emergency-contacts/{cid} 긴급연락처 삭제
 */
public class PatientHandler extends BaseHandler {

    private final PatientController patientController;

    public PatientHandler(PatientController patientController) {
        this.patientController = patientController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        // parts: ["", "api", "patients", {id}, ...]
        String[] parts = path.split("/");
        if (parts.length < 4) { sendError(ex, 404, "Not Found"); return; }
        String patientId = parts[3];

        if ("GET".equals(method) && parts.length == 4) {
            handleGetPatient(ex, patientId);
        } else if ("PUT".equals(method) && parts.length == 5 && "profile".equals(parts[4])) {
            handleSaveProfile(ex, patientId);
        } else if ("POST".equals(method) && parts.length == 6 && "onboarding".equals(parts[4]) && "submit".equals(parts[5])) {
            handleSubmitOnboarding(ex, patientId);
        } else if ("POST".equals(method) && parts.length == 6 && "onboarding".equals(parts[4]) && "approve".equals(parts[5])) {
            handleApproveOnboarding(ex, patientId);
        } else if ("POST".equals(method) && parts.length == 5 && "questionnaire".equals(parts[4])) {
            handleSaveQuestionnaire(ex, patientId);
        } else if ("GET".equals(method) && parts.length == 5 && "questionnaire".equals(parts[4])) {
            handleGetQuestionnaire(ex, patientId);
        } else if ("POST".equals(method) && parts.length == 5 && "emergency-contacts".equals(parts[4])) {
            handleAddContact(ex, patientId);
        } else if ("GET".equals(method) && parts.length == 5 && "emergency-contacts".equals(parts[4])) {
            handleGetContacts(ex, patientId);
        } else if ("DELETE".equals(method) && parts.length == 6 && "emergency-contacts".equals(parts[4])) {
            handleRemoveContact(ex, patientId, parts[5]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleSaveProfile(HttpExchange ex, String patientId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        PatientDTO dto = new PatientDTO();
        dto.setUserId(patientId);
        dto.setFullNameEn(body.get("fullNameEn"));
        dto.setNationality(body.get("nationality"));
        String dob = body.get("dateOfBirth");
        if (dob != null && !dob.isEmpty()) dto.setDateOfBirth(LocalDate.parse(dob));
        PatientDTO result = patientController.savePatientProfile(dto);
        sendJson(ex, 200, patientToMap(result));
    }

    private void handleGetPatient(HttpExchange ex, String id) throws IOException {
        PatientDTO result = patientController.getPatient(id);
        sendJson(ex, 200, patientToMap(result));
    }

    private void handleSubmitOnboarding(HttpExchange ex, String id) throws IOException {
        PatientDTO result = patientController.submitOnboarding(id);
        sendJson(ex, 200, patientToMap(result));
    }

    private void handleApproveOnboarding(HttpExchange ex, String id) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        PatientDTO result = patientController.approveOnboarding(id, body.get("adminId"));
        sendJson(ex, 200, patientToMap(result));
    }

    private void handleSaveQuestionnaire(HttpExchange ex, String patientId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        MedicalQuestionnaireDTO dto = new MedicalQuestionnaireDTO();
        dto.setPatientId(patientId);
        if (body.get("currentMedications") != null) dto.setCurrentMedications(Arrays.asList(body.get("currentMedications").split(",")));
        if (body.get("allergies") != null) dto.setAllergies(Arrays.asList(body.get("allergies").split(",")));
        if (body.get("pastSurgeries") != null) dto.setPastSurgeries(Arrays.asList(body.get("pastSurgeries").split(",")));
        dto.setMedicalNotes(body.get("medicalNotes"));
        MedicalQuestionnaireDTO result = patientController.saveMedicalQuestionnaire(dto);
        sendJson(ex, 201, questionnaireToMap(result));
    }

    private void handleGetQuestionnaire(HttpExchange ex, String patientId) throws IOException {
        MedicalQuestionnaireDTO result = patientController.getMedicalQuestionnaire(patientId);
        sendJson(ex, 200, questionnaireToMap(result));
    }

    private void handleAddContact(HttpExchange ex, String patientId) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        EmergencyContactDTO dto = new EmergencyContactDTO();
        dto.setPatientId(patientId);
        dto.setFullNameEn(body.get("fullNameEn"));
        dto.setRelationship(body.get("relationship"));
        dto.setPhoneE164(body.get("phoneE164"));
        EmergencyContactDTO result = patientController.addEmergencyContact(dto);
        sendJson(ex, 201, contactToMap(result));
    }

    private void handleGetContacts(HttpExchange ex, String patientId) throws IOException {
        List<EmergencyContactDTO> list = patientController.getEmergencyContacts(patientId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (EmergencyContactDTO c : list) items.add(contactToMap(c));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size());
        resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private void handleRemoveContact(HttpExchange ex, String patientId, String contactId) throws IOException {
        patientController.removeEmergencyContact(patientId, contactId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("result", "removed");
        sendJson(ex, 200, m);
    }

    private Map<String, Object> patientToMap(PatientDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId",            dto.getUserId());
        m.put("email",             dto.getEmail());
        m.put("fullNameEn",        dto.getFullNameEn());
        m.put("dateOfBirth",       dto.getDateOfBirth() != null ? dto.getDateOfBirth().toString() : null);
        m.put("nationality",       dto.getNationality());
        m.put("onboardingStatus",  dto.getOnboardingStatus() != null ? dto.getOnboardingStatus().name() : null);
        m.put("preferredLanguage", dto.getPreferredLanguage() != null ? dto.getPreferredLanguage().name() : null);
        return m;
    }

    private Map<String, Object> questionnaireToMap(MedicalQuestionnaireDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("questionnaireId",     dto.getQuestionnaireId());
        m.put("patientId",           dto.getPatientId());
        m.put("currentMedications",  dto.getCurrentMedications());
        m.put("allergies",           dto.getAllergies());
        m.put("pastSurgeries",       dto.getPastSurgeries());
        m.put("medicalNotes",        dto.getMedicalNotes());
        m.put("submittedAt",         dto.getSubmittedAt() != null ? dto.getSubmittedAt().toString() : null);
        return m;
    }

    private Map<String, Object> contactToMap(EmergencyContactDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("contactId",    dto.getContactId());
        m.put("patientId",    dto.getPatientId());
        m.put("fullNameEn",   dto.getFullNameEn());
        m.put("relationship", dto.getRelationship());
        m.put("phoneE164",    dto.getPhoneE164());
        m.put("sortOrder",    dto.getSortOrder());
        return m;
    }
}
