package com.k.medtour.domain.journey.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k.medtour.domain.journey.dto.LocationDto;
import com.k.medtour.domain.journey.dto.NavigationResponse;
import com.k.medtour.domain.journey.dto.PatientNoticeResponse;
import com.k.medtour.domain.journey.dto.StaffTodayResponse;
import com.k.medtour.domain.journey.dto.StatusUpdateRequest;
import com.k.medtour.domain.journey.dto.StatusUpdateResponse;
import com.k.medtour.domain.journey.enums.ScheduleItemStatus;
import com.k.medtour.domain.journey.service.StaffAssignmentService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StaffJourneyController.class)
@AutoConfigureMockMvc(addFilters = false)
class StaffJourneyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StaffAssignmentService staffAssignmentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        SecurityTestUtil.setAuthentication(10L, "STAFF");
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtil.clearAuthentication();
    }

    @Nested
    @DisplayName("당일 업무 API")
    class TodayTasksApiTest {

        @Test
        @DisplayName("GET /staff/me/today - 당일 업무 조회")
        void getTodayTasks() throws Exception {
            StaffTodayResponse response = new StaffTodayResponse(
                    LocalDate.of(2026, 4, 16), 2, 1,
                    List.of(new StaffTodayResponse.TaskDto(
                            200L, 505L, 100L,
                            LocalDateTime.of(2026, 4, 16, 10, 0),
                            "병원 진료", "MEDICAL", "COMPLETED",
                            new StaffTodayResponse.PatientDto(5L, "John Doe", "en"),
                            new LocationDto("클리닉", "강남구", 37.52, 127.05, null),
                            180, LocalDateTime.of(2026, 4, 16, 13, 0))));

            given(staffAssignmentService.getTodayTasks(any(), any())).willReturn(response);

            mockMvc.perform(get("/api/v1/journeys/staff/me/today"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalTasks").value(2));
        }
    }

    @Nested
    @DisplayName("환자 특이사항 API")
    class PatientNoticeApiTest {

        @Test
        @DisplayName("GET /{journeyId}/patient-notice - 환자 특이사항 조회")
        void getPatientNotice() throws Exception {
            PatientNoticeResponse response = new PatientNoticeResponse(
                    5L, "John Doe", "en", "en",
                    List.of("Penicillin"), "할랄 식단",
                    new PatientNoticeResponse.EmergencyContactDto("Jane", "SPOUSE", "+1-555"));

            given(staffAssignmentService.getPatientNotice(eq(100L), any())).willReturn(response);

            mockMvc.perform(get("/api/v1/journeys/100/patient-notice"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.patientName").value("John Doe"))
                    .andExpect(jsonPath("$.data.allergies[0]").value("Penicillin"));
        }

        @Test
        @DisplayName("GET /{journeyId}/patient-notice - 배정 안 된 실무자 403")
        void getPatientNotice_NotAssigned() throws Exception {
            given(staffAssignmentService.getPatientNotice(eq(100L), any()))
                    .willThrow(new BusinessException(ErrorCode.STAFF_NOT_ASSIGNED));

            mockMvc.perform(get("/api/v1/journeys/100/patient-notice"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("상태 업데이트 API")
    class StatusUpdateApiTest {

        @Test
        @DisplayName("PATCH /.../status - 상태 업데이트 성공")
        void updateStatus() throws Exception {
            StatusUpdateRequest request = new StatusUpdateRequest(ScheduleItemStatus.EN_ROUTE, "이동 시작");

            StatusUpdateResponse response = new StatusUpdateResponse(
                    505L, ScheduleItemStatus.SCHEDULED, ScheduleItemStatus.EN_ROUTE,
                    LocalDateTime.now(),
                    new StatusUpdateResponse.UpdatedByDto(10L, "Kim Driver"));

            given(staffAssignmentService.updateStatus(eq(100L), eq(505L), any(), any()))
                    .willReturn(response);

            mockMvc.perform(patch("/api/v1/journeys/100/schedule-items/505/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.previousStatus").value("SCHEDULED"))
                    .andExpect(jsonPath("$.data.newStatus").value("EN_ROUTE"));
        }

        @Test
        @DisplayName("PATCH /.../status - 잘못된 상태 전이 400")
        void updateStatus_InvalidTransition() throws Exception {
            StatusUpdateRequest request = new StatusUpdateRequest(ScheduleItemStatus.COMPLETED, null);

            given(staffAssignmentService.updateStatus(eq(100L), eq(505L), any(), any()))
                    .willThrow(new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION));

            mockMvc.perform(patch("/api/v1/journeys/100/schedule-items/505/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("네비게이션 딥링크 API")
    class NavigationApiTest {

        @Test
        @DisplayName("GET /.../navigation - 딥링크 조회")
        void getNavigation() throws Exception {
            NavigationResponse response = new NavigationResponse(
                    new NavigationResponse.DestinationDto("클리닉", 37.5172, 127.0473),
                    Map.of(
                            "google", "https://www.google.com/maps/dir/?api=1&destination=37.5172,127.0473",
                            "kakao", "kakaomap://route?ep=37.5172,127.0473",
                            "naver", "nmap://route/car?dlat=37.5172&dlng=127.0473&dname=클리닉",
                            "apple", "maps://?daddr=37.5172,127.0473"
                    ));

            given(staffAssignmentService.getNavigation(eq(100L), eq(505L), any())).willReturn(response);

            mockMvc.perform(get("/api/v1/journeys/100/schedule-items/505/navigation"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.destination.name").value("클리닉"))
                    .andExpect(jsonPath("$.data.deepLinks.google").exists());
        }
    }
}
