package com.k.medtour.domain.journey.service;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.admin.entity.Role;
import com.k.medtour.domain.admin.repository.MemberRepository;
import com.k.medtour.domain.journey.dto.JourneyCreateRequest;
import com.k.medtour.domain.journey.dto.JourneyDetailResponse;
import com.k.medtour.domain.journey.dto.JourneyResponse;
import com.k.medtour.domain.journey.dto.LocationDto;
import com.k.medtour.domain.journey.dto.ScheduleItemCreateRequest;
import com.k.medtour.domain.journey.dto.ScheduleItemResponse;
import com.k.medtour.domain.journey.dto.ScheduleItemUpdateRequest;
import com.k.medtour.domain.journey.dto.TemplateCreateRequest;
import com.k.medtour.domain.journey.dto.TemplateItemDto;
import com.k.medtour.domain.journey.dto.TemplateListResponse;
import com.k.medtour.domain.journey.dto.TemplateResponse;
import com.k.medtour.domain.journey.dto.TimelineResponse;
import com.k.medtour.domain.journey.entity.Journey;
import com.k.medtour.domain.journey.entity.JourneyScheduleItem;
import com.k.medtour.domain.journey.entity.JourneyTemplate;
import com.k.medtour.domain.journey.entity.JourneyTemplateItem;
import com.k.medtour.domain.journey.enums.JourneyStatus;
import com.k.medtour.domain.journey.enums.ScheduleItemStatus;
import com.k.medtour.domain.journey.enums.ScheduleItemType;
import com.k.medtour.domain.journey.enums.TemplateCategory;
import com.k.medtour.domain.journey.repository.JourneyRepository;
import com.k.medtour.domain.journey.repository.JourneyScheduleItemRepository;
import com.k.medtour.domain.journey.repository.JourneyTemplateRepository;
import com.k.medtour.domain.journey.repository.StaffAssignmentRepository;
import com.k.medtour.domain.staff.repository.StaffProfileRepository;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JourneyServiceTest {

    @InjectMocks
    private JourneyService journeyService;

    @Mock
    private JourneyTemplateRepository templateRepository;
    @Mock
    private JourneyRepository journeyRepository;
    @Mock
    private JourneyScheduleItemRepository scheduleItemRepository;
    @Mock
    private StaffAssignmentRepository staffAssignmentRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private StaffProfileRepository staffProfileRepository;

    private Role patientRole;
    private Member patient;
    private JourneyTemplate template;

    @BeforeEach
    void setUp() {
        patientRole = Role.builder().name("PATIENT").description("환자").build();
        ReflectionTestUtils.setField(patientRole, "id", 3L);

        patient = Member.builder()
                .email("john@example.com")
                .name("John Doe")
                .role(patientRole)
                .language("en")
                .build();
        ReflectionTestUtils.setField(patient, "id", 5L);

        template = JourneyTemplate.builder()
                .name("서울 성형 VIP 7일")
                .category(TemplateCategory.MIXED)
                .durationDays(7)
                .build();
        ReflectionTestUtils.setField(template, "id", 1L);
        ReflectionTestUtils.setField(template, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(template, "updatedAt", LocalDateTime.now());

        JourneyTemplateItem item = JourneyTemplateItem.builder()
                .dayOffset(0)
                .timeOffset("09:00")
                .title("공항 픽업")
                .type(ScheduleItemType.TRANSPORT)
                .description("VIP 픽업")
                .durationMinutes(90)
                .location(Map.of("name", "인천공항", "address", "인천", "latitude", 37.46, "longitude", 126.44))
                .requiredStaff(List.of("DRIVER"))
                .sortOrder(1)
                .build();
        item.assignTemplate(template);
        template.getItems().add(item);
    }

    @Nested
    @DisplayName("템플릿 CRUD")
    class TemplateCrudTest {

        @Test
        @DisplayName("템플릿 생성 성공")
        void createTemplate_Success() {
            TemplateCreateRequest request = new TemplateCreateRequest(
                    "테스트 템플릿",
                    TemplateCategory.SURGERY,
                    5,
                    List.of(new TemplateItemDto(null, 0, "09:00", "진료", ScheduleItemType.MEDICAL,
                            "초진", 120, new LocationDto("병원", "주소", 37.0, 127.0, null),
                            List.of("INTERPRETER"), 1))
            );

            given(templateRepository.existsByName("테스트 템플릿")).willReturn(false);
            given(templateRepository.save(any(JourneyTemplate.class))).willAnswer(invocation -> {
                JourneyTemplate t = invocation.getArgument(0);
                ReflectionTestUtils.setField(t, "id", 2L);
                ReflectionTestUtils.setField(t, "createdAt", LocalDateTime.now());
                ReflectionTestUtils.setField(t, "updatedAt", LocalDateTime.now());
                return t;
            });

            TemplateResponse response = journeyService.createTemplate(request);

            assertThat(response.name()).isEqualTo("테스트 템플릿");
            assertThat(response.category()).isEqualTo(TemplateCategory.SURGERY);
            verify(templateRepository).save(any(JourneyTemplate.class));
        }

        @Test
        @DisplayName("중복 이름 템플릿 생성 실패")
        void createTemplate_DuplicateName() {
            TemplateCreateRequest request = new TemplateCreateRequest(
                    "중복 이름", TemplateCategory.TOUR, 3, List.of());

            given(templateRepository.existsByName("중복 이름")).willReturn(true);

            assertThatThrownBy(() -> journeyService.createTemplate(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TEMPLATE_DUPLICATE_NAME);
        }

        @Test
        @DisplayName("템플릿 목록 조회 성공")
        void getTemplates_Success() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<JourneyTemplate> page = new PageImpl<>(List.of(template), pageable, 1);

            given(templateRepository.findAllByFilters(null, null, pageable)).willReturn(page);

            PageResponse<TemplateListResponse> response = journeyService.getTemplates(null, null, pageable);

            assertThat(response.content()).hasSize(1);
            assertThat(response.content().getFirst().name()).isEqualTo("서울 성형 VIP 7일");
        }

        @Test
        @DisplayName("템플릿 상세 조회 성공")
        void getTemplate_Success() {
            given(templateRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(template));

            TemplateResponse response = journeyService.getTemplate(1L);

            assertThat(response.name()).isEqualTo("서울 성형 VIP 7일");
            assertThat(response.items()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 템플릿 조회 실패")
        void getTemplate_NotFound() {
            given(templateRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> journeyService.getTemplate(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TEMPLATE_NOT_FOUND);
        }

        @Test
        @DisplayName("템플릿 수정 성공")
        void updateTemplate_Success() {
            TemplateCreateRequest request = new TemplateCreateRequest(
                    "수정된 이름", TemplateCategory.RECOVERY, 5, List.of());

            given(templateRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(template));
            given(templateRepository.existsByNameAndIdNot("수정된 이름", 1L)).willReturn(false);

            TemplateResponse response = journeyService.updateTemplate(1L, request);

            assertThat(response.name()).isEqualTo("수정된 이름");
            assertThat(response.category()).isEqualTo(TemplateCategory.RECOVERY);
        }

        @Test
        @DisplayName("템플릿 삭제 성공")
        void deleteTemplate_Success() {
            given(templateRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(template));

            journeyService.deleteTemplate(1L);

            assertThat(template.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("여정 CRUD")
    class JourneyCrudTest {

        @Test
        @DisplayName("여정 생성 성공")
        void createJourney_Success() {
            JourneyCreateRequest request = new JourneyCreateRequest(
                    5L, 1L, LocalDate.of(2026, 4, 15), "John Doe VIP", "특이사항");

            given(memberRepository.findById(5L)).willReturn(Optional.of(patient));
            given(templateRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(template));
            given(journeyRepository.existsByPatientIdAndStatusIn(eq(5L), any())).willReturn(false);
            given(journeyRepository.save(any(Journey.class))).willAnswer(invocation -> {
                Journey j = invocation.getArgument(0);
                ReflectionTestUtils.setField(j, "id", 100L);
                ReflectionTestUtils.setField(j, "createdAt", LocalDateTime.now());
                return j;
            });

            JourneyResponse response = journeyService.createJourney(request);

            assertThat(response.title()).isEqualTo("John Doe VIP");
            assertThat(response.status()).isEqualTo(JourneyStatus.PLANNED);
            assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 4, 15));
            assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 4, 21));
        }

        @Test
        @DisplayName("이미 활성 여정이 있는 환자에게 여정 생성 실패")
        void createJourney_ActiveExists() {
            JourneyCreateRequest request = new JourneyCreateRequest(
                    5L, 1L, LocalDate.of(2026, 4, 15), "Test", null);

            given(memberRepository.findById(5L)).willReturn(Optional.of(patient));
            given(templateRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(template));
            given(journeyRepository.existsByPatientIdAndStatusIn(eq(5L), any())).willReturn(true);

            assertThatThrownBy(() -> journeyService.createJourney(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ACTIVE_JOURNEY_EXISTS);
        }

        @Test
        @DisplayName("여정 상세 조회 - ADMIN 성공")
        void getJourneyDetail_Admin_Success() {
            Journey journey = Journey.builder()
                    .patient(patient).title("Test").startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(6)).notes("note").build();
            ReflectionTestUtils.setField(journey, "id", 100L);
            ReflectionTestUtils.setField(journey, "createdAt", LocalDateTime.now());

            UserPrincipal principal = new UserPrincipal(1L, "ADMIN");

            given(journeyRepository.findByIdAndDeletedAtIsNull(100L)).willReturn(Optional.of(journey));

            JourneyDetailResponse response = journeyService.getJourneyDetail(100L, principal);

            assertThat(response.id()).isEqualTo(100L);
        }

        @Test
        @DisplayName("여정 상세 조회 - PATIENT 본인 아닌 경우 실패")
        void getJourneyDetail_PatientAccessDenied() {
            Journey journey = Journey.builder()
                    .patient(patient).title("Test").startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(6)).build();
            ReflectionTestUtils.setField(journey, "id", 100L);

            UserPrincipal principal = new UserPrincipal(999L, "PATIENT");

            given(journeyRepository.findByIdAndDeletedAtIsNull(100L)).willReturn(Optional.of(journey));

            assertThatThrownBy(() -> journeyService.getJourneyDetail(100L, principal))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_ACCESS_DENIED);
        }
    }

    @Nested
    @DisplayName("일정 항목 CRUD")
    class ScheduleItemTest {

        private Journey journey;

        @BeforeEach
        void setUp() {
            journey = Journey.builder()
                    .patient(patient).title("Test")
                    .startDate(LocalDate.of(2026, 4, 15))
                    .endDate(LocalDate.of(2026, 4, 21))
                    .build();
            ReflectionTestUtils.setField(journey, "id", 100L);
            ReflectionTestUtils.setField(journey, "createdAt", LocalDateTime.now());
        }

        @Test
        @DisplayName("일정 항목 추가 성공")
        void addScheduleItem_Success() {
            ScheduleItemCreateRequest request = new ScheduleItemCreateRequest(
                    LocalDateTime.of(2026, 4, 17, 14, 0),
                    "쇼핑 투어", ScheduleItemType.TOUR, "관광", 180,
                    new LocationDto("명동", "서울시 중구", 37.56, 126.99, null),
                    List.of("DRIVER", "INTERPRETER"));

            given(journeyRepository.findByIdAndDeletedAtIsNull(100L)).willReturn(Optional.of(journey));
            given(scheduleItemRepository.save(any(JourneyScheduleItem.class))).willAnswer(inv -> {
                JourneyScheduleItem item = inv.getArgument(0);
                ReflectionTestUtils.setField(item, "id", 510L);
                ReflectionTestUtils.setField(item, "createdAt", LocalDateTime.now());
                ReflectionTestUtils.setField(item, "updatedAt", LocalDateTime.now());
                return item;
            });

            ScheduleItemResponse response = journeyService.addScheduleItem(100L, request);

            assertThat(response.title()).isEqualTo("쇼핑 투어");
            assertThat(response.type()).isEqualTo(ScheduleItemType.TOUR);
            assertThat(response.status()).isEqualTo(ScheduleItemStatus.SCHEDULED);
        }

        @Test
        @DisplayName("일정 항목 수정 성공")
        void updateScheduleItem_Success() {
            JourneyScheduleItem item = JourneyScheduleItem.builder()
                    .journey(journey).dayNumber(3)
                    .scheduledAt(LocalDateTime.of(2026, 4, 17, 14, 0))
                    .title("원래 제목").type(ScheduleItemType.TOUR)
                    .durationMinutes(180).build();
            ReflectionTestUtils.setField(item, "id", 510L);
            ReflectionTestUtils.setField(item, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(item, "updatedAt", LocalDateTime.now());

            ScheduleItemUpdateRequest request = new ScheduleItemUpdateRequest(
                    null, "수정된 제목", "새 설명", 120, null, "변경 사유");

            given(scheduleItemRepository.findByIdAndJourneyId(510L, 100L)).willReturn(Optional.of(item));

            ScheduleItemResponse response = journeyService.updateScheduleItem(100L, 510L, request);

            assertThat(response.title()).isEqualTo("수정된 제목");
        }

        @Test
        @DisplayName("완료된 일정 수정 시 실패")
        void updateScheduleItem_Completed_Fail() {
            JourneyScheduleItem item = JourneyScheduleItem.builder()
                    .journey(journey).dayNumber(1)
                    .scheduledAt(LocalDateTime.of(2026, 4, 15, 9, 0))
                    .title("완료 일정").type(ScheduleItemType.TRANSPORT)
                    .build();
            ReflectionTestUtils.setField(item, "id", 500L);
            ReflectionTestUtils.setField(item, "status", ScheduleItemStatus.COMPLETED);

            ScheduleItemUpdateRequest request = new ScheduleItemUpdateRequest(
                    null, "수정", null, null, null, null);

            given(scheduleItemRepository.findByIdAndJourneyId(500L, 100L)).willReturn(Optional.of(item));

            assertThatThrownBy(() -> journeyService.updateScheduleItem(100L, 500L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SCHEDULE_ITEM_COMPLETED);
        }
    }

    @Nested
    @DisplayName("타임라인 조회")
    class TimelineTest {

        @Test
        @DisplayName("환자 타임라인 조회 성공")
        void getTimeline_Success() {
            Journey journey = Journey.builder()
                    .patient(patient).title("Test Journey")
                    .startDate(LocalDate.of(2026, 4, 15))
                    .endDate(LocalDate.of(2026, 4, 21))
                    .build();
            ReflectionTestUtils.setField(journey, "id", 100L);

            given(journeyRepository.findByPatientIdAndStatusIn(eq(5L), any()))
                    .willReturn(List.of(journey));

            JourneyScheduleItem item = JourneyScheduleItem.builder()
                    .journey(journey).dayNumber(2)
                    .scheduledAt(LocalDateTime.of(2026, 4, 16, 10, 0))
                    .title("병원 진료").type(ScheduleItemType.MEDICAL)
                    .description("초진").durationMinutes(180)
                    .location(Map.of("name", "클리닉", "latitude", 37.52, "longitude", 127.05))
                    .build();
            ReflectionTestUtils.setField(item, "id", 505L);

            LocalDate targetDate = LocalDate.of(2026, 4, 16);
            given(scheduleItemRepository.findByJourneyIdAndDate(eq(100L), any(), any()))
                    .willReturn(List.of(item));

            TimelineResponse response = journeyService.getTimeline(5L, targetDate);

            assertThat(response.journeyId()).isEqualTo(100L);
            assertThat(response.dayNumber()).isEqualTo(2);
            assertThat(response.items()).hasSize(1);
            assertThat(response.items().getFirst().title()).isEqualTo("병원 진료");
        }

        @Test
        @DisplayName("활성 여정이 없을 때 실패")
        void getTimeline_NoActiveJourney() {
            given(journeyRepository.findByPatientIdAndStatusIn(eq(5L), any()))
                    .willReturn(List.of());

            assertThatThrownBy(() -> journeyService.getTimeline(5L, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_NOT_FOUND);
        }
    }
}
