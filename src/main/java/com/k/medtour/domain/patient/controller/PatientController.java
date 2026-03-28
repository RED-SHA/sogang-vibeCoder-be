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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "환자", description = "환자 온보딩(여권, 문진표, 긴급연락처) 및 환자 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // ========== Passport ==========

    @Operation(summary = "여권 정보 등록", description = "환자 본인의 여권 정보 신규 등록")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "여권 정보 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/patients/me/passport")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PassportResponse>> createPassport(
            @Valid @RequestBody PassportRequest request) {
        PassportResponse response = patientService.createPassport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "여권 정보 조회", description = "특정 환자의 여권 정보 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여권 정보 없음")
    })
    @GetMapping("/patients/{patientId}/passport")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<PassportResponse>> getPassport(
            @Parameter(description = "환자 ID", required = true) @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPassport(patientId)));
    }

    @Operation(summary = "여권 정보 수정", description = "환자 본인의 여권 정보 수정")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PutMapping("/patients/me/passport")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PassportResponse>> updatePassport(
            @Valid @RequestBody PassportRequest request) {
        return ResponseEntity.ok(ApiResponse.success(patientService.updatePassport(request)));
    }

    // ========== Medical Questionnaire ==========

    @Operation(summary = "의료 문진표 등록", description = "환자 본인의 영문 의료/알레르기 문진표 등록")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "문진표 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/patients/me/medical-questionnaire")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<QuestionnaireResponse>> createQuestionnaire(
            @Valid @RequestBody QuestionnaireRequest request) {
        QuestionnaireResponse response = patientService.createQuestionnaire(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "의료 문진표 조회", description = "특정 환자의 의료 문진표 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문진표 없음")
    })
    @GetMapping("/patients/{patientId}/medical-questionnaire")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<QuestionnaireResponse>> getQuestionnaire(
            @Parameter(description = "환자 ID", required = true) @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getQuestionnaire(patientId)));
    }

    @Operation(summary = "의료 문진표 수정", description = "환자 본인의 의료 문진표 수정")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PutMapping("/patients/me/medical-questionnaire")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<QuestionnaireResponse>> updateQuestionnaire(
            @Valid @RequestBody QuestionnaireRequest request) {
        return ResponseEntity.ok(ApiResponse.success(patientService.updateQuestionnaire(request)));
    }

    // ========== Emergency Contact ==========

    @Operation(summary = "긴급 연락처 등록", description = "환자 본인의 긴급 연락처 신규 등록")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "긴급 연락처 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/patients/me/emergency-contacts")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> createEmergencyContact(
            @Valid @RequestBody EmergencyContactRequest request) {
        EmergencyContactResponse response = patientService.createEmergencyContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "긴급 연락처 목록 조회", description = "특정 환자의 긴급 연락처 목록 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/patients/{patientId}/emergency-contacts")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<List<EmergencyContactResponse>>> getEmergencyContacts(
            @Parameter(description = "환자 ID", required = true) @PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getEmergencyContacts(patientId)));
    }

    @Operation(summary = "긴급 연락처 수정", description = "환자 본인의 특정 긴급 연락처 수정")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "연락처를 찾을 수 없음")
    })
    @PutMapping("/patients/me/emergency-contacts/{contactId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> updateEmergencyContact(
            @Parameter(description = "연락처 ID", required = true) @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContactRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                patientService.updateEmergencyContact(contactId, request)));
    }

    @Operation(summary = "긴급 연락처 삭제", description = "환자 본인의 특정 긴급 연락처 삭제")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "연락처를 찾을 수 없음")
    })
    @DeleteMapping("/patients/me/emergency-contacts/{contactId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Void>> deleteEmergencyContact(
            @Parameter(description = "연락처 ID", required = true) @PathVariable Long contactId) {
        patientService.deleteEmergencyContact(contactId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ========== Admin ==========

    @Operation(summary = "환자 목록 조회", description = "관리자용 전체 환자 목록 페이징 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/admin/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<Page<PatientListResponse>>> getPatientList(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientList(pageable)));
    }
}
