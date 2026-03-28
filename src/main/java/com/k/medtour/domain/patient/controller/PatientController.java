package com.k.medtour.domain.patient.controller;

import com.k.medtour.domain.patient.dto.EmergencyContactRequest;
import com.k.medtour.domain.patient.dto.EmergencyContactResponse;
import com.k.medtour.domain.patient.dto.PassportRequest;
import com.k.medtour.domain.patient.dto.PassportResponse;
import com.k.medtour.domain.patient.dto.PatientListResponse;
import com.k.medtour.domain.patient.dto.QuestionnaireRequest;
import com.k.medtour.domain.patient.dto.QuestionnaireResponse;
import com.k.medtour.domain.patient.service.PatientService;
import com.k.medtour.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // ========== Passport ==========

    @PostMapping("/patients/me/passport")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PassportResponse>> createPassport(
            @Valid @RequestBody PassportRequest request) {
        PassportResponse response = patientService.createPassport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/patients/{patientId}/passport")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<PassportResponse>> getPassport(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPassport(patientId)));
    }

    @PutMapping("/patients/me/passport")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PassportResponse>> updatePassport(
            @Valid @RequestBody PassportRequest request) {
        return ResponseEntity.ok(ApiResponse.success(patientService.updatePassport(request)));
    }

    // ========== Medical Questionnaire ==========

    @PostMapping("/patients/me/medical-questionnaire")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<QuestionnaireResponse>> createQuestionnaire(
            @Valid @RequestBody QuestionnaireRequest request) {
        QuestionnaireResponse response = patientService.createQuestionnaire(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/patients/{patientId}/medical-questionnaire")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<QuestionnaireResponse>> getQuestionnaire(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getQuestionnaire(patientId)));
    }

    @PutMapping("/patients/me/medical-questionnaire")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<QuestionnaireResponse>> updateQuestionnaire(
            @Valid @RequestBody QuestionnaireRequest request) {
        return ResponseEntity.ok(ApiResponse.success(patientService.updateQuestionnaire(request)));
    }

    // ========== Emergency Contact ==========

    @PostMapping("/patients/me/emergency-contacts")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> createEmergencyContact(
            @Valid @RequestBody EmergencyContactRequest request) {
        EmergencyContactResponse response = patientService.createEmergencyContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/patients/{patientId}/emergency-contacts")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<List<EmergencyContactResponse>>> getEmergencyContacts(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getEmergencyContacts(patientId)));
    }

    @PutMapping("/patients/me/emergency-contacts/{contactId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> updateEmergencyContact(
            @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContactRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                patientService.updateEmergencyContact(contactId, request)));
    }

    @DeleteMapping("/patients/me/emergency-contacts/{contactId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Void>> deleteEmergencyContact(
            @PathVariable Long contactId) {
        patientService.deleteEmergencyContact(contactId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ========== Admin ==========

    @GetMapping("/admin/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<Page<PatientListResponse>>> getPatientList(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientList(pageable)));
    }
}
