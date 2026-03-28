package com.k.medtour.domain.journey.service;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.admin.entity.Role;
import com.k.medtour.domain.admin.repository.MemberRepository;
import com.k.medtour.domain.journey.dto.NavigationResponse;
import com.k.medtour.domain.journey.dto.PatientNoticeResponse;
import com.k.medtour.domain.journey.dto.StaffTodayResponse;
import com.k.medtour.domain.journey.dto.StatusUpdateRequest;
import com.k.medtour.domain.journey.dto.StatusUpdateResponse;
import com.k.medtour.domain.journey.entity.Journey;
import com.k.medtour.domain.journey.entity.JourneyScheduleItem;
import com.k.medtour.domain.journey.entity.StaffAssignment;
import com.k.medtour.domain.journey.enums.JourneyStatus;
import com.k.medtour.domain.journey.enums.ScheduleItemStatus;
import com.k.medtour.domain.journey.enums.ScheduleItemType;
import com.k.medtour.domain.journey.enums.StaffAssignmentStatus;
import com.k.medtour.domain.journey.repository.JourneyRepository;
import com.k.medtour.domain.journey.repository.JourneyScheduleItemRepository;
import com.k.medtour.domain.journey.repository.StaffAssignmentRepository;
import com.k.medtour.domain.patient.entity.EmergencyContact;
import com.k.medtour.domain.patient.entity.MedicalQuestionnaire;
import com.k.medtour.domain.patient.enums.BloodType;
import com.k.medtour.domain.patient.repository.EmergencyContactRepository;
import com.k.medtour.domain.patient.repository.MedicalQuestionnaireRepository;
import com.k.medtour.domain.staff.repository.StaffProfileRepository;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StaffAssignmentServiceTest {

    @InjectMocks
    private StaffAssignmentService staffAssignmentService;

    @Mock
    private StaffAssignmentRepository staffAssignmentRepository;
    @Mock
    private JourneyScheduleItemRepository scheduleItemRepository;
    @Mock
    private JourneyRepository journeyRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private StaffProfileRepository staffProfileRepository;
    @Mock
    private MedicalQuestionnaireRepository questionnaireRepository;
    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    private Role patientRole;
    private Role staffRole;
    private Member patient;
    private Member staffMember;
    private Journey journey;
    private JourneyScheduleItem scheduleItem;
    private StaffAssignment assignment;

    @BeforeEach
    void setUp() {
        patientRole = Role.builder().name("PATIENT").description("환자").build();
        ReflectionTestUtils.setField(patientRole, "id", 3L);

        staffRole = Role.builder().name("STAFF").description("실무자").build();
        ReflectionTestUtils.setField(staffRole, "id", 4L);

        patient = Member.builder()
                .email("john@example.com").name("John Doe")
                .role(patientRole).language("en").phone("+1-555-0100")
                .build();
        ReflectionTestUtils.setField(patient, "id", 5L);

        staffMember = Member.builder()
                .email("driver@example.com").name("Kim Driver")
                .role(staffRole).language("ko").phone("+82-10-1234")
                .build();
        ReflectionTestUtils.setField(staffMember, "id", 10L);

        journey = Journey.builder()
                .patient(patient).title("Test Journey")
                .startDate(LocalDate.of(2026, 4, 15))
                .endDate(LocalDate.of(2026, 4, 21))
                .notes("할랄 식단")
                .build();
        ReflectionTestUtils.setField(journey, "id", 100L);

        scheduleItem = JourneyScheduleItem.builder()
                .journey(journey).dayNumber(2)
                .scheduledAt(LocalDateTime.of(2026, 4, 16, 10, 0))
                .title("병원 진료").type(ScheduleItemType.MEDICAL)
                .durationMinutes(180)
                .location(Map.of("name", "클리닉", "address", "강남구",
                        "latitude", 37.5172, "longitude", 127.0473))
                .build();
        ReflectionTestUtils.setField(scheduleItem, "id", 505L);

        assignment = StaffAssignment.builder()
                .scheduleItem(scheduleItem).staff(staffMember).build();
        ReflectionTestUtils.setField(assignment, "id", 200L);
    }

    @Nested
    @DisplayName("당일 업무 조회")
    class TodayTasksTest {

        @Test
        @DisplayName("당일 업무 조회 성공")
        void getTodayTasks_Success() {
            LocalDate date = LocalDate.of(2026, 4, 16);
            given(staffAssignmentRepository.findByStaffIdAndDate(eq(10L), any(), any()))
                    .willReturn(List.of(assignment));

            StaffTodayResponse response = staffAssignmentService.getTodayTasks(10L, date);

            assertThat(response.totalTasks()).isEqualTo(1);
            assertThat(response.tasks().getFirst().title()).isEqualTo("병원 진료");
            assertThat(response.tasks().getFirst().patient().name()).isEqualTo("John Doe");
        }
    }

    @Nested
    @DisplayName("상태 업데이트")
    class StatusUpdateTest {

        @Test
        @DisplayName("상태 업데이트 성공 (SCHEDULED -> EN_ROUTE)")
        void updateStatus_Success() {
            StatusUpdateRequest request = new StatusUpdateRequest(ScheduleItemStatus.EN_ROUTE, "이동 시작");

            given(scheduleItemRepository.findByIdAndJourneyId(505L, 100L))
                    .willReturn(Optional.of(scheduleItem));
            given(staffAssignmentRepository.findByScheduleItemIdAndStaffId(505L, 10L))
                    .willReturn(Optional.of(assignment));
            given(memberRepository.findById(10L)).willReturn(Optional.of(staffMember));

            StatusUpdateResponse response = staffAssignmentService.updateStatus(100L, 505L, 10L, request);

            assertThat(response.previousStatus()).isEqualTo(ScheduleItemStatus.SCHEDULED);
            assertThat(response.newStatus()).isEqualTo(ScheduleItemStatus.EN_ROUTE);
            assertThat(response.updatedBy().name()).isEqualTo("Kim Driver");
        }

        @Test
        @DisplayName("잘못된 상태 전이 실패 (SCHEDULED -> COMPLETED)")
        void updateStatus_InvalidTransition() {
            StatusUpdateRequest request = new StatusUpdateRequest(ScheduleItemStatus.COMPLETED, null);

            given(scheduleItemRepository.findByIdAndJourneyId(505L, 100L))
                    .willReturn(Optional.of(scheduleItem));
            given(staffAssignmentRepository.findByScheduleItemIdAndStaffId(505L, 10L))
                    .willReturn(Optional.of(assignment));

            assertThatThrownBy(() -> staffAssignmentService.updateStatus(100L, 505L, 10L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("배정되지 않은 실무자 상태 업데이트 실패")
        void updateStatus_NotAssigned() {
            StatusUpdateRequest request = new StatusUpdateRequest(ScheduleItemStatus.EN_ROUTE, null);

            given(scheduleItemRepository.findByIdAndJourneyId(505L, 100L))
                    .willReturn(Optional.of(scheduleItem));
            given(staffAssignmentRepository.findByScheduleItemIdAndStaffId(505L, 10L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> staffAssignmentService.updateStatus(100L, 505L, 10L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STAFF_NOT_ASSIGNED);
        }
    }

    @Nested
    @DisplayName("환자 특이사항 조회")
    class PatientNoticeTest {

        @Test
        @DisplayName("환자 특이사항 조회 성공")
        void getPatientNotice_Success() {
            given(staffAssignmentRepository.existsByJourneyIdAndStaffId(100L, 10L)).willReturn(true);
            given(journeyRepository.findByIdAndDeletedAtIsNull(100L)).willReturn(Optional.of(journey));

            MedicalQuestionnaire questionnaire = MedicalQuestionnaire.builder()
                    .member(patient).bloodType(BloodType.A_POSITIVE)
                    .allergies(List.of("Penicillin")).build();
            given(questionnaireRepository.findByMemberIdAndDeletedAtIsNull(5L))
                    .willReturn(Optional.of(questionnaire));

            EmergencyContact contact = EmergencyContact.builder()
                    .member(patient).name("Jane Doe").relationship("SPOUSE")
                    .phone("+1-555-0200").isPrimary(true).build();
            given(emergencyContactRepository.findAllByMemberIdAndDeletedAtIsNull(5L))
                    .willReturn(List.of(contact));

            PatientNoticeResponse response = staffAssignmentService.getPatientNotice(100L, 10L);

            assertThat(response.patientName()).isEqualTo("John Doe");
            assertThat(response.allergies()).contains("Penicillin");
            assertThat(response.emergencyContact().name()).isEqualTo("Jane Doe");
            assertThat(response.specialNotes()).isEqualTo("할랄 식단");
        }

        @Test
        @DisplayName("배정되지 않은 실무자 조회 실패")
        void getPatientNotice_NotAssigned() {
            given(staffAssignmentRepository.existsByJourneyIdAndStaffId(100L, 10L)).willReturn(false);

            assertThatThrownBy(() -> staffAssignmentService.getPatientNotice(100L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STAFF_NOT_ASSIGNED);
        }
    }

    @Nested
    @DisplayName("네비게이션 딥링크")
    class NavigationTest {

        @Test
        @DisplayName("딥링크 생성 성공")
        void getNavigation_Success() {
            given(scheduleItemRepository.findByIdAndJourneyId(505L, 100L))
                    .willReturn(Optional.of(scheduleItem));

            NavigationResponse response = staffAssignmentService.getNavigation(100L, 505L, "google");

            assertThat(response.destination().name()).isEqualTo("클리닉");
            assertThat(response.deepLinks()).containsKey("google");
            assertThat(response.deepLinks().get("google"))
                    .contains("37.5172").contains("127.0473");
            assertThat(response.deepLinks()).containsKey("kakao");
            assertThat(response.deepLinks()).containsKey("naver");
            assertThat(response.deepLinks()).containsKey("apple");
        }
    }
}
