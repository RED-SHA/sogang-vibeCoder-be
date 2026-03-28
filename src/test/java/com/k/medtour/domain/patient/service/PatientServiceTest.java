package com.k.medtour.domain.patient.service;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.admin.entity.Role;
import com.k.medtour.domain.admin.repository.MemberRepository;
import com.k.medtour.domain.patient.dto.EmergencyContactRequest;
import com.k.medtour.domain.patient.dto.EmergencyContactResponse;
import com.k.medtour.domain.patient.dto.PassportRequest;
import com.k.medtour.domain.patient.dto.PassportResponse;
import com.k.medtour.domain.patient.dto.QuestionnaireRequest;
import com.k.medtour.domain.patient.dto.QuestionnaireResponse;
import com.k.medtour.domain.patient.entity.EmergencyContact;
import com.k.medtour.domain.patient.entity.MedicalQuestionnaire;
import com.k.medtour.domain.patient.entity.PatientPassport;
import com.k.medtour.domain.patient.enums.BloodType;
import com.k.medtour.domain.patient.enums.Gender;
import com.k.medtour.domain.patient.enums.PassportInputType;
import com.k.medtour.domain.patient.enums.VerificationStatus;
import com.k.medtour.domain.patient.repository.EmergencyContactRepository;
import com.k.medtour.domain.patient.repository.MedicalQuestionnaireRepository;
import com.k.medtour.domain.patient.repository.PatientPassportRepository;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @InjectMocks
    private PatientService patientService;

    @Mock
    private PatientPassportRepository passportRepository;
    @Mock
    private MedicalQuestionnaireRepository questionnaireRepository;
    @Mock
    private EmergencyContactRepository emergencyContactRepository;
    @Mock
    private MemberRepository memberRepository;

    private Member testMember;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder().name("PATIENT").description("환자").build();
        testMember = Member.builder()
                .email("patient@test.com")
                .name("Test Patient")
                .role(testRole)
                .language("en")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(Long memberId, String role) {
        UserPrincipal principal = new UserPrincipal(memberId, role);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ========== Passport Tests ==========

    @Nested
    @DisplayName("여권 정보 등록")
    class CreatePassportTest {

        @Test
        @DisplayName("성공 - 여권 정보를 정상적으로 등록한다")
        void createPassport_success() {
            // Given
            setSecurityContext(1L, "PATIENT");
            PassportRequest request = new PassportRequest(
                    "M12345678", "John Doe", "US",
                    LocalDate.of(1990, 1, 1), LocalDate.of(2030, 1, 1),
                    Gender.MALE, PassportInputType.MANUAL, null, null
            );

            given(passportRepository.existsByMemberIdAndDeletedAtIsNull(1L)).willReturn(false);
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(passportRepository.save(any(PatientPassport.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            PassportResponse response = patientService.createPassport(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.fullName()).isEqualTo("John Doe");
            assertThat(response.passportNumber()).isEqualTo("M1234****");
            assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.PENDING);
            verify(passportRepository).save(any(PatientPassport.class));
        }

        @Test
        @DisplayName("실패 - 이미 여권 정보가 존재하면 예외가 발생한다")
        void createPassport_fail_alreadyExists() {
            // Given
            setSecurityContext(1L, "PATIENT");
            PassportRequest request = new PassportRequest(
                    "M12345678", "John Doe", "US",
                    LocalDate.of(1990, 1, 1), LocalDate.of(2030, 1, 1),
                    Gender.MALE, PassportInputType.MANUAL, null, null
            );

            given(passportRepository.existsByMemberIdAndDeletedAtIsNull(1L)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> patientService.createPassport(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PASSPORT_ALREADY_EXISTS));
        }
    }

    @Nested
    @DisplayName("여권 정보 조회")
    class GetPassportTest {

        @Test
        @DisplayName("성공 - 본인의 여권 정보를 조회한다")
        void getPassport_success_ownData() {
            // Given
            setSecurityContext(1L, "PATIENT");
            PatientPassport passport = PatientPassport.builder()
                    .member(testMember)
                    .passportNumber("M12345678")
                    .fullName("John Doe")
                    .nationality("US")
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .expiryDate(LocalDate.of(2030, 1, 1))
                    .gender(Gender.MALE)
                    .inputType(PassportInputType.MANUAL)
                    .build();

            given(passportRepository.findByMemberIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(passport));

            // When
            PassportResponse response = patientService.getPassport(1L);

            // Then
            assertThat(response.fullName()).isEqualTo("John Doe");
            assertThat(response.passportNumber()).isEqualTo("M1234****");
        }

        @Test
        @DisplayName("성공 - 관리자가 환자의 여권 정보를 조회한다")
        void getPassport_success_admin() {
            // Given
            setSecurityContext(99L, "ADMIN");
            PatientPassport passport = PatientPassport.builder()
                    .member(testMember)
                    .passportNumber("M12345678")
                    .fullName("John Doe")
                    .nationality("US")
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .expiryDate(LocalDate.of(2030, 1, 1))
                    .gender(Gender.MALE)
                    .inputType(PassportInputType.MANUAL)
                    .build();

            given(passportRepository.findByMemberIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(passport));

            // When
            PassportResponse response = patientService.getPassport(1L);

            // Then
            assertThat(response.fullName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("실패 - 다른 환자의 여권 정보 접근 시 예외가 발생한다")
        void getPassport_fail_accessDenied() {
            // Given
            setSecurityContext(2L, "PATIENT");

            // When & Then
            assertThatThrownBy(() -> patientService.getPassport(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PATIENT_ACCESS_DENIED));
        }

        @Test
        @DisplayName("실패 - 여권 정보가 없으면 예외가 발생한다")
        void getPassport_fail_notFound() {
            // Given
            setSecurityContext(1L, "PATIENT");
            given(passportRepository.findByMemberIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> patientService.getPassport(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PASSPORT_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("여권 정보 수정")
    class UpdatePassportTest {

        @Test
        @DisplayName("성공 - 여권 정보를 정상적으로 수정한다")
        void updatePassport_success() {
            // Given
            setSecurityContext(1L, "PATIENT");
            PatientPassport passport = PatientPassport.builder()
                    .member(testMember)
                    .passportNumber("M12345678")
                    .fullName("John Doe")
                    .nationality("US")
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .expiryDate(LocalDate.of(2030, 1, 1))
                    .gender(Gender.MALE)
                    .inputType(PassportInputType.MANUAL)
                    .build();

            PassportRequest request = new PassportRequest(
                    "M87654321", "Jane Doe", "UK",
                    LocalDate.of(1991, 2, 2), LocalDate.of(2031, 2, 2),
                    Gender.FEMALE, PassportInputType.OCR, null, 0.95
            );

            given(passportRepository.findByMemberIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(passport));

            // When
            PassportResponse response = patientService.updatePassport(request);

            // Then
            assertThat(response.fullName()).isEqualTo("Jane Doe");
            assertThat(response.nationality()).isEqualTo("UK");
        }

        @Test
        @DisplayName("실패 - 수정할 여권 정보가 없으면 예외가 발생한다")
        void updatePassport_fail_notFound() {
            // Given
            setSecurityContext(1L, "PATIENT");
            PassportRequest request = new PassportRequest(
                    "M87654321", "Jane Doe", "UK",
                    LocalDate.of(1991, 2, 2), LocalDate.of(2031, 2, 2),
                    Gender.FEMALE, PassportInputType.OCR, null, 0.95
            );

            given(passportRepository.findByMemberIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> patientService.updatePassport(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PASSPORT_NOT_FOUND));
        }
    }

    // ========== Questionnaire Tests ==========

    @Nested
    @DisplayName("문진표 등록")
    class CreateQuestionnaireTest {

        @Test
        @DisplayName("성공 - 문진표를 정상적으로 등록한다")
        void createQuestionnaire_success() {
            // Given
            setSecurityContext(1L, "PATIENT");
            QuestionnaireRequest request = new QuestionnaireRequest(
                    BloodType.A_POSITIVE, 175.0, 70.0,
                    List.of("Penicillin"), List.of("Aspirin"),
                    List.of("Appendectomy"), List.of("Asthma"),
                    "No additional notes"
            );

            given(questionnaireRepository.existsByMemberIdAndDeletedAtIsNull(1L)).willReturn(false);
            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(questionnaireRepository.save(any(MedicalQuestionnaire.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            QuestionnaireResponse response = patientService.createQuestionnaire(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.bloodType()).isEqualTo(BloodType.A_POSITIVE);
            assertThat(response.allergies()).containsExactly("Penicillin");
            verify(questionnaireRepository).save(any(MedicalQuestionnaire.class));
        }

        @Test
        @DisplayName("실패 - 이미 문진표가 존재하면 예외가 발생한다")
        void createQuestionnaire_fail_alreadyExists() {
            // Given
            setSecurityContext(1L, "PATIENT");
            QuestionnaireRequest request = new QuestionnaireRequest(
                    BloodType.A_POSITIVE, 175.0, 70.0,
                    List.of(), List.of(), List.of(), List.of(), null
            );

            given(questionnaireRepository.existsByMemberIdAndDeletedAtIsNull(1L)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> patientService.createQuestionnaire(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.QUESTIONNAIRE_ALREADY_EXISTS));
        }
    }

    @Nested
    @DisplayName("문진표 조회")
    class GetQuestionnaireTest {

        @Test
        @DisplayName("성공 - 본인의 문진표를 조회한다")
        void getQuestionnaire_success() {
            // Given
            setSecurityContext(1L, "PATIENT");
            MedicalQuestionnaire questionnaire = MedicalQuestionnaire.builder()
                    .member(testMember)
                    .bloodType(BloodType.B_POSITIVE)
                    .height(180.0)
                    .weight(75.0)
                    .allergies(List.of("Dust"))
                    .currentMedications(List.of())
                    .pastSurgeries(List.of())
                    .chronicConditions(List.of())
                    .build();

            given(questionnaireRepository.findByMemberIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(questionnaire));

            // When
            QuestionnaireResponse response = patientService.getQuestionnaire(1L);

            // Then
            assertThat(response.bloodType()).isEqualTo(BloodType.B_POSITIVE);
            assertThat(response.height()).isEqualTo(180.0);
        }

        @Test
        @DisplayName("실패 - 문진표가 없으면 예외가 발생한다")
        void getQuestionnaire_fail_notFound() {
            // Given
            setSecurityContext(1L, "PATIENT");
            given(questionnaireRepository.findByMemberIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> patientService.getQuestionnaire(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.QUESTIONNAIRE_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("문진표 수정")
    class UpdateQuestionnaireTest {

        @Test
        @DisplayName("성공 - 문진표를 정상적으로 수정한다")
        void updateQuestionnaire_success() {
            // Given
            setSecurityContext(1L, "PATIENT");
            MedicalQuestionnaire questionnaire = MedicalQuestionnaire.builder()
                    .member(testMember)
                    .bloodType(BloodType.A_POSITIVE)
                    .height(175.0)
                    .weight(70.0)
                    .allergies(List.of())
                    .currentMedications(List.of())
                    .pastSurgeries(List.of())
                    .chronicConditions(List.of())
                    .build();

            QuestionnaireRequest request = new QuestionnaireRequest(
                    BloodType.O_NEGATIVE, 176.0, 72.0,
                    List.of("Peanuts"), List.of(), List.of(), List.of(), "Updated"
            );

            given(questionnaireRepository.findByMemberIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(questionnaire));

            // When
            QuestionnaireResponse response = patientService.updateQuestionnaire(request);

            // Then
            assertThat(response.bloodType()).isEqualTo(BloodType.O_NEGATIVE);
            assertThat(response.additionalNotes()).isEqualTo("Updated");
        }
    }

    // ========== Emergency Contact Tests ==========

    @Nested
    @DisplayName("긴급 연락처 등록")
    class CreateEmergencyContactTest {

        @Test
        @DisplayName("성공 - 긴급 연락처를 정상적으로 등록한다")
        void createEmergencyContact_success() {
            // Given
            setSecurityContext(1L, "PATIENT");
            EmergencyContactRequest request = new EmergencyContactRequest(
                    "Jane Doe", "Spouse", "+1-555-1234", "jane@test.com", true
            );

            given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
            given(emergencyContactRepository.save(any(EmergencyContact.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            EmergencyContactResponse response = patientService.createEmergencyContact(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Jane Doe");
            assertThat(response.relationship()).isEqualTo("Spouse");
            assertThat(response.isPrimary()).isTrue();
            verify(emergencyContactRepository).save(any(EmergencyContact.class));
        }
    }

    @Nested
    @DisplayName("긴급 연락처 조회")
    class GetEmergencyContactsTest {

        @Test
        @DisplayName("성공 - 본인의 긴급 연락처 목록을 조회한다")
        void getEmergencyContacts_success() {
            // Given
            setSecurityContext(1L, "PATIENT");
            EmergencyContact contact1 = EmergencyContact.builder()
                    .member(testMember)
                    .name("Jane Doe")
                    .relationship("Spouse")
                    .phone("+1-555-1234")
                    .isPrimary(true)
                    .build();
            EmergencyContact contact2 = EmergencyContact.builder()
                    .member(testMember)
                    .name("Bob Doe")
                    .relationship("Parent")
                    .phone("+1-555-5678")
                    .isPrimary(false)
                    .build();

            given(emergencyContactRepository.findAllByMemberIdAndDeletedAtIsNull(1L))
                    .willReturn(List.of(contact1, contact2));

            // When
            List<EmergencyContactResponse> responses = patientService.getEmergencyContacts(1L);

            // Then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).name()).isEqualTo("Jane Doe");
            assertThat(responses.get(1).name()).isEqualTo("Bob Doe");
        }

        @Test
        @DisplayName("실패 - 다른 환자의 긴급 연락처 접근 시 예외가 발생한다")
        void getEmergencyContacts_fail_accessDenied() {
            // Given
            setSecurityContext(2L, "PATIENT");

            // When & Then
            assertThatThrownBy(() -> patientService.getEmergencyContacts(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PATIENT_ACCESS_DENIED));
        }
    }

    @Nested
    @DisplayName("긴급 연락처 수정")
    class UpdateEmergencyContactTest {

        @Test
        @DisplayName("성공 - 긴급 연락처를 정상적으로 수정한다")
        void updateEmergencyContact_success() {
            // Given
            setSecurityContext(1L, "PATIENT");
            EmergencyContact contact = EmergencyContact.builder()
                    .member(testMember)
                    .name("Jane Doe")
                    .relationship("Spouse")
                    .phone("+1-555-1234")
                    .isPrimary(true)
                    .build();

            EmergencyContactRequest request = new EmergencyContactRequest(
                    "Jane Smith", "Spouse", "+1-555-9999", "jane.smith@test.com", true
            );

            given(emergencyContactRepository.findByIdAndMemberIdAndDeletedAtIsNull(10L, 1L))
                    .willReturn(Optional.of(contact));

            // When
            EmergencyContactResponse response = patientService.updateEmergencyContact(10L, request);

            // Then
            assertThat(response.name()).isEqualTo("Jane Smith");
            assertThat(response.phone()).isEqualTo("+1-555-9999");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 긴급 연락처 수정 시 예외가 발생한다")
        void updateEmergencyContact_fail_notFound() {
            // Given
            setSecurityContext(1L, "PATIENT");
            EmergencyContactRequest request = new EmergencyContactRequest(
                    "Jane Smith", "Spouse", "+1-555-9999", null, true
            );

            given(emergencyContactRepository.findByIdAndMemberIdAndDeletedAtIsNull(999L, 1L))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> patientService.updateEmergencyContact(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.EMERGENCY_CONTACT_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("긴급 연락처 삭제")
    class DeleteEmergencyContactTest {

        @Test
        @DisplayName("성공 - 긴급 연락처를 정상적으로 삭제한다")
        void deleteEmergencyContact_success() {
            // Given
            setSecurityContext(1L, "PATIENT");
            EmergencyContact contact = EmergencyContact.builder()
                    .member(testMember)
                    .name("Jane Doe")
                    .relationship("Spouse")
                    .phone("+1-555-1234")
                    .isPrimary(false)
                    .build();

            given(emergencyContactRepository.findByIdAndMemberIdAndDeletedAtIsNull(10L, 1L))
                    .willReturn(Optional.of(contact));

            // When
            patientService.deleteEmergencyContact(10L);

            // Then
            assertThat(contact.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 긴급 연락처 삭제 시 예외가 발생한다")
        void deleteEmergencyContact_fail_notFound() {
            // Given
            setSecurityContext(1L, "PATIENT");

            given(emergencyContactRepository.findByIdAndMemberIdAndDeletedAtIsNull(999L, 1L))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> patientService.deleteEmergencyContact(999L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.EMERGENCY_CONTACT_NOT_FOUND));
        }
    }
}
