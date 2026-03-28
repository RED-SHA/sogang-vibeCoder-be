package com.k.medtour.domain.admin.service;

import com.k.medtour.domain.admin.dto.DashboardOverviewResponse;
import com.k.medtour.domain.admin.dto.StaffStatusResponse;
import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.admin.entity.Role;
import com.k.medtour.domain.admin.repository.MemberRepository;
import com.k.medtour.domain.chat.repository.ChatMessageRepository;
import com.k.medtour.domain.journey.entity.Journey;
import com.k.medtour.domain.journey.enums.JourneyStatus;
import com.k.medtour.domain.journey.repository.JourneyRepository;
import com.k.medtour.domain.journey.repository.JourneyScheduleItemRepository;
import com.k.medtour.domain.journey.repository.StaffAssignmentRepository;
import com.k.medtour.domain.staff.entity.StaffProfile;
import com.k.medtour.domain.staff.enums.StaffType;
import com.k.medtour.domain.staff.repository.StaffProfileRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JourneyRepository journeyRepository;

    @Mock
    private JourneyScheduleItemRepository scheduleItemRepository;

    @Mock
    private StaffAssignmentRepository staffAssignmentRepository;

    @Mock
    private StaffProfileRepository staffProfileRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Nested
    @DisplayName("getOverview")
    class GetOverviewTest {

        @Test
        @DisplayName("성공 - 대시보드 통계를 반환한다")
        void getOverview_success() {
            // Given
            Page<Member> patientPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 1), 10);
            given(memberRepository.findAllByRoleName(eq("PATIENT"), any(PageRequest.class)))
                    .willReturn(patientPage);

            Page<Journey> journeyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 1), 5);
            given(journeyRepository.findAllByFilters(
                    eq(JourneyStatus.IN_PROGRESS), any(), any(), any(), any(PageRequest.class)))
                    .willReturn(journeyPage);

            given(scheduleItemRepository.countByScheduledAtBetweenAndDeletedAtIsNull(
                    any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(3L);

            given(chatMessageRepository.countUnreadMessages()).willReturn(7L);

            // When
            DashboardOverviewResponse result = dashboardService.getOverview();

            // Then
            assertThat(result.totalPatients()).isEqualTo(10);
            assertThat(result.activeJourneys()).isEqualTo(5);
            assertThat(result.todaySchedules()).isEqualTo(3);
            assertThat(result.unreadChats()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("getStaffStatus")
    class GetStaffStatusTest {

        @Test
        @DisplayName("성공 - 실무자 현황을 반환한다")
        void getStaffStatus_success() {
            // Given
            Role staffRole = Role.builder().name("STAFF").description("Staff").build();
            Member staffMember = Member.builder()
                    .email("staff@test.com").name("Driver Kim").role(staffRole).language("ko").build();
            StaffProfile staffProfile = StaffProfile.builder()
                    .member(staffMember).staffType(StaffType.DRIVER).isAvailable(true).build();

            given(staffProfileRepository.findAll()).willReturn(List.of(staffProfile));
            given(staffAssignmentRepository.findByStaffIdAndDate(any(), any(), any()))
                    .willReturn(Collections.emptyList());

            // When
            StaffStatusResponse result = dashboardService.getStaffStatus();

            // Then
            assertThat(result.totalStaff()).isEqualTo(1);
            assertThat(result.available()).isEqualTo(1);
            assertThat(result.onDuty()).isEqualTo(0);
            assertThat(result.offline()).isEqualTo(0);
        }

        @Test
        @DisplayName("성공 - 비활성 실무자는 OFFLINE으로 표시")
        void getStaffStatus_offline() {
            // Given
            Role staffRole = Role.builder().name("STAFF").description("Staff").build();
            Member staffMember = Member.builder()
                    .email("staff@test.com").name("Driver Kim").role(staffRole).language("ko").build();
            StaffProfile staffProfile = StaffProfile.builder()
                    .member(staffMember).staffType(StaffType.DRIVER).isAvailable(false).build();

            given(staffProfileRepository.findAll()).willReturn(List.of(staffProfile));

            // When
            StaffStatusResponse result = dashboardService.getStaffStatus();

            // Then
            assertThat(result.offline()).isEqualTo(1);
        }
    }
}
