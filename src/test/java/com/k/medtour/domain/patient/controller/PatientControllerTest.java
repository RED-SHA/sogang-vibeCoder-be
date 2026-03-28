package com.k.medtour.domain.patient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k.medtour.domain.patient.dto.EmergencyContactRequest;
import com.k.medtour.domain.patient.dto.EmergencyContactResponse;
import com.k.medtour.domain.patient.dto.PassportRequest;
import com.k.medtour.domain.patient.dto.PassportResponse;
import com.k.medtour.domain.patient.dto.QuestionnaireRequest;
import com.k.medtour.domain.patient.dto.QuestionnaireResponse;
import com.k.medtour.domain.patient.enums.BloodType;
import com.k.medtour.domain.patient.enums.Gender;
import com.k.medtour.domain.patient.enums.PassportInputType;
import com.k.medtour.domain.patient.enums.QuestionnaireStatus;
import com.k.medtour.domain.patient.enums.VerificationStatus;
import com.k.medtour.domain.patient.service.PatientService;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import com.k.medtour.support.SecurityTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        SecurityTestUtil.setAuthentication(1L, "PATIENT");
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtil.clearAuthentication();
    }

    // ========== Passport ==========

    @Nested
    @DisplayName("POST /api/v1/patients/me/passport")
    class CreatePassportApiTest {

        @Test
        @DisplayName("성공 - 여권 정보를 등록하면 201을 반환한다")
        void createPassport_success() throws Exception {
            // Given
            PassportRequest request = new PassportRequest(
                    "M12345678", "John Doe", "US",
                    LocalDate.of(1990, 1, 1), LocalDate.of(2030, 1, 1),
                    Gender.MALE, PassportInputType.MANUAL, null, null
            );
            PassportResponse response = new PassportResponse(
                    1L, 1L, "M1234****", "John Doe", "US",
                    LocalDate.of(1990, 1, 1), LocalDate.of(2030, 1, 1),
                    Gender.MALE, PassportInputType.MANUAL, null, null,
                    VerificationStatus.PENDING, null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(patientService.createPassport(any(PassportRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/patients/me/passport")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.passportNumber").value("M1234****"))
                    .andExpect(jsonPath("$.data.fullName").value("John Doe"));
        }

        @Test
        @DisplayName("실패 - 이미 여권이 존재하면 409를 반환한다")
        void createPassport_fail_conflict() throws Exception {
            // Given
            PassportRequest request = new PassportRequest(
                    "M12345678", "John Doe", "US",
                    LocalDate.of(1990, 1, 1), LocalDate.of(2030, 1, 1),
                    Gender.MALE, PassportInputType.MANUAL, null, null
            );

            given(patientService.createPassport(any(PassportRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.PASSPORT_ALREADY_EXISTS));

            // When & Then
            mockMvc.perform(post("/api/v1/patients/me/passport")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/patients/{patientId}/passport")
    class GetPassportApiTest {

        @Test
        @DisplayName("성공 - 여권 정보를 정상 조회한다")
        void getPassport_success() throws Exception {
            // Given
            PassportResponse response = new PassportResponse(
                    1L, 1L, "M1234****", "John Doe", "US",
                    LocalDate.of(1990, 1, 1), LocalDate.of(2030, 1, 1),
                    Gender.MALE, PassportInputType.MANUAL, null, null,
                    VerificationStatus.PENDING, null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(patientService.getPassport(1L)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/patients/1/passport"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.fullName").value("John Doe"));
        }

        @Test
        @DisplayName("실패 - 여권 정보가 없으면 404를 반환한다")
        void getPassport_fail_notFound() throws Exception {
            // Given
            given(patientService.getPassport(1L))
                    .willThrow(new BusinessException(ErrorCode.PASSPORT_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/patients/1/passport"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/patients/me/passport")
    class UpdatePassportApiTest {

        @Test
        @DisplayName("성공 - 여권 정보를 수정하면 200을 반환한다")
        void updatePassport_success() throws Exception {
            // Given
            PassportRequest request = new PassportRequest(
                    "M87654321", "Jane Doe", "UK",
                    LocalDate.of(1991, 2, 2), LocalDate.of(2031, 2, 2),
                    Gender.FEMALE, PassportInputType.OCR, null, 0.95
            );
            PassportResponse response = new PassportResponse(
                    1L, 1L, "M8765****", "Jane Doe", "UK",
                    LocalDate.of(1991, 2, 2), LocalDate.of(2031, 2, 2),
                    Gender.FEMALE, PassportInputType.OCR, null, 0.95,
                    VerificationStatus.PENDING, null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(patientService.updatePassport(any(PassportRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/patients/me/passport")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.fullName").value("Jane Doe"));
        }
    }

    // ========== Questionnaire ==========

    @Nested
    @DisplayName("POST /api/v1/patients/me/medical-questionnaire")
    class CreateQuestionnaireApiTest {

        @Test
        @DisplayName("성공 - 문진표를 등록하면 201을 반환한다")
        void createQuestionnaire_success() throws Exception {
            // Given
            QuestionnaireRequest request = new QuestionnaireRequest(
                    BloodType.A_POSITIVE, 175.0, 70.0,
                    List.of("Penicillin"), List.of("Aspirin"),
                    List.of("Appendectomy"), List.of("Asthma"),
                    "No notes"
            );
            QuestionnaireResponse response = new QuestionnaireResponse(
                    1L, 1L, BloodType.A_POSITIVE, 175.0, 70.0,
                    List.of("Penicillin"), List.of("Aspirin"),
                    List.of("Appendectomy"), List.of("Asthma"),
                    "No notes", QuestionnaireStatus.SUBMITTED,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(patientService.createQuestionnaire(any(QuestionnaireRequest.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/patients/me/medical-questionnaire")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.bloodType").value("A_POSITIVE"));
        }

        @Test
        @DisplayName("실패 - 이미 문진표가 존재하면 409를 반환한다")
        void createQuestionnaire_fail_conflict() throws Exception {
            // Given
            QuestionnaireRequest request = new QuestionnaireRequest(
                    BloodType.A_POSITIVE, 175.0, 70.0,
                    List.of(), List.of(), List.of(), List.of(), null
            );

            given(patientService.createQuestionnaire(any(QuestionnaireRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.QUESTIONNAIRE_ALREADY_EXISTS));

            // When & Then
            mockMvc.perform(post("/api/v1/patients/me/medical-questionnaire")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/patients/{patientId}/medical-questionnaire")
    class GetQuestionnaireApiTest {

        @Test
        @DisplayName("성공 - 문진표를 정상 조회한다")
        void getQuestionnaire_success() throws Exception {
            // Given
            QuestionnaireResponse response = new QuestionnaireResponse(
                    1L, 1L, BloodType.B_POSITIVE, 180.0, 75.0,
                    List.of("Dust"), List.of(), List.of(), List.of(),
                    null, QuestionnaireStatus.SUBMITTED,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(patientService.getQuestionnaire(1L)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/patients/1/medical-questionnaire"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.bloodType").value("B_POSITIVE"));
        }
    }

    // ========== Emergency Contact ==========

    @Nested
    @DisplayName("POST /api/v1/patients/me/emergency-contacts")
    class CreateEmergencyContactApiTest {

        @Test
        @DisplayName("성공 - 긴급 연락처를 등록하면 201을 반환한다")
        void createEmergencyContact_success() throws Exception {
            // Given
            EmergencyContactRequest request = new EmergencyContactRequest(
                    "Jane Doe", "Spouse", "+1-555-1234", "jane@test.com", true
            );
            EmergencyContactResponse response = new EmergencyContactResponse(
                    1L, 1L, "Jane Doe", "Spouse", "+1-555-1234",
                    "jane@test.com", true,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(patientService.createEmergencyContact(any(EmergencyContactRequest.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/patients/me/emergency-contacts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Jane Doe"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/patients/{patientId}/emergency-contacts")
    class GetEmergencyContactsApiTest {

        @Test
        @DisplayName("성공 - 긴급 연락처 목록을 조회한다")
        void getEmergencyContacts_success() throws Exception {
            // Given
            List<EmergencyContactResponse> responses = List.of(
                    new EmergencyContactResponse(1L, 1L, "Jane Doe", "Spouse",
                            "+1-555-1234", "jane@test.com", true,
                            LocalDateTime.now(), LocalDateTime.now())
            );

            given(patientService.getEmergencyContacts(1L)).willReturn(responses);

            // When & Then
            mockMvc.perform(get("/api/v1/patients/1/emergency-contacts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].name").value("Jane Doe"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/patients/me/emergency-contacts/{contactId}")
    class UpdateEmergencyContactApiTest {

        @Test
        @DisplayName("성공 - 긴급 연락처를 수정하면 200을 반환한다")
        void updateEmergencyContact_success() throws Exception {
            // Given
            EmergencyContactRequest request = new EmergencyContactRequest(
                    "Jane Smith", "Spouse", "+1-555-9999", "jane.smith@test.com", true
            );
            EmergencyContactResponse response = new EmergencyContactResponse(
                    1L, 1L, "Jane Smith", "Spouse", "+1-555-9999",
                    "jane.smith@test.com", true,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(patientService.updateEmergencyContact(eq(1L), any(EmergencyContactRequest.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/patients/me/emergency-contacts/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Jane Smith"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/patients/me/emergency-contacts/{contactId}")
    class DeleteEmergencyContactApiTest {

        @Test
        @DisplayName("성공 - 긴급 연락처를 삭제하면 200을 반환한다")
        void deleteEmergencyContact_success() throws Exception {
            // Given
            doNothing().when(patientService).deleteEmergencyContact(1L);

            // When & Then
            mockMvc.perform(delete("/api/v1/patients/me/emergency-contacts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 긴급 연락처 삭제 시 404를 반환한다")
        void deleteEmergencyContact_fail_notFound() throws Exception {
            // Given
            doThrow(new BusinessException(ErrorCode.EMERGENCY_CONTACT_NOT_FOUND))
                    .when(patientService).deleteEmergencyContact(999L);

            // When & Then
            mockMvc.perform(delete("/api/v1/patients/me/emergency-contacts/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
