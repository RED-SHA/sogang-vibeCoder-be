package com.k.medtour.domain.journey.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k.medtour.domain.journey.dto.JourneyCreateRequest;
import com.k.medtour.domain.journey.dto.JourneyDetailResponse;
import com.k.medtour.domain.journey.dto.JourneyListResponse;
import com.k.medtour.domain.journey.dto.JourneyResponse;
import com.k.medtour.domain.journey.dto.LocationDto;
import com.k.medtour.domain.journey.dto.ScheduleItemCreateRequest;
import com.k.medtour.domain.journey.dto.ScheduleItemResponse;
import com.k.medtour.domain.journey.dto.ScheduleItemUpdateRequest;
import com.k.medtour.domain.journey.dto.StaffAssignRequest;
import com.k.medtour.domain.journey.dto.TemplateCreateRequest;
import com.k.medtour.domain.journey.dto.TemplateItemDto;
import com.k.medtour.domain.journey.dto.TemplateListResponse;
import com.k.medtour.domain.journey.dto.TemplateResponse;
import com.k.medtour.domain.journey.dto.TimelineResponse;
import com.k.medtour.domain.journey.enums.JourneyStatus;
import com.k.medtour.domain.journey.enums.ScheduleItemStatus;
import com.k.medtour.domain.journey.enums.ScheduleItemType;
import com.k.medtour.domain.journey.enums.TemplateCategory;
import com.k.medtour.domain.journey.service.JourneyService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import com.k.medtour.global.common.PageResponse;
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

@WebMvcTest(JourneyController.class)
@AutoConfigureMockMvc(addFilters = false)
class JourneyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JourneyService journeyService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        SecurityTestUtil.setAuthentication(1L, "ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtil.clearAuthentication();
    }

    @Nested
    @DisplayName("템플릿 API")
    class TemplateApiTest {

        @Test
        @DisplayName("GET /templates - 템플릿 목록 조회")
        void getTemplates() throws Exception {
            TemplateListResponse item = new TemplateListResponse(
                    1L, "VIP 패키지", TemplateCategory.MIXED, 7, 15, 35,
                    LocalDateTime.of(2026, 1, 15, 9, 0));
            PageResponse<TemplateListResponse> response = new PageResponse<>(
                    List.of(item), 0, 20, 1, 1);

            given(journeyService.getTemplates(any(), any(), any())).willReturn(response);

            mockMvc.perform(get("/api/v1/journeys/templates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].name").value("VIP 패키지"));
        }

        @Test
        @DisplayName("POST /templates - 템플릿 생성")
        void createTemplate() throws Exception {
            TemplateCreateRequest request = new TemplateCreateRequest(
                    "신규 템플릿", TemplateCategory.SURGERY, 5,
                    List.of(new TemplateItemDto(null, 0, "09:00", "진료",
                            ScheduleItemType.MEDICAL, "초진", 120,
                            new LocationDto("병원", "주소", 37.0, 127.0, null),
                            List.of("INTERPRETER"), 1)));

            TemplateResponse responseDto = new TemplateResponse(
                    2L, "신규 템플릿", TemplateCategory.SURGERY, 5,
                    List.of(), 0, LocalDateTime.now(), LocalDateTime.now());

            given(journeyService.createTemplate(any())).willReturn(responseDto);

            mockMvc.perform(post("/api/v1/journeys/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("신규 템플릿"));
        }

        @Test
        @DisplayName("POST /templates - 중복 이름 409")
        void createTemplate_Duplicate() throws Exception {
            TemplateCreateRequest request = new TemplateCreateRequest(
                    "중복", TemplateCategory.TOUR, 3, List.of());

            given(journeyService.createTemplate(any()))
                    .willThrow(new BusinessException(ErrorCode.TEMPLATE_DUPLICATE_NAME));

            mockMvc.perform(post("/api/v1/journeys/templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("DELETE /templates/{id} - 삭제 성공")
        void deleteTemplate() throws Exception {
            doNothing().when(journeyService).deleteTemplate(1L);

            mockMvc.perform(delete("/api/v1/journeys/templates/1"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("여정 API")
    class JourneyApiTest {

        @Test
        @DisplayName("POST /journeys - 여정 생성")
        void createJourney() throws Exception {
            JourneyCreateRequest request = new JourneyCreateRequest(
                    5L, 1L, LocalDate.of(2026, 4, 15), "John VIP", "notes");

            JourneyResponse responseDto = new JourneyResponse(
                    100L, 5L, "John VIP", JourneyStatus.PLANNED,
                    LocalDate.of(2026, 4, 15), LocalDate.of(2026, 4, 21),
                    15, LocalDateTime.now());

            given(journeyService.createJourney(any())).willReturn(responseDto);

            mockMvc.perform(post("/api/v1/journeys")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(100))
                    .andExpect(jsonPath("$.data.status").value("PLANNED"));
        }

        @Test
        @DisplayName("GET /journeys - 여정 목록 조회")
        void getJourneys() throws Exception {
            PageResponse<JourneyListResponse> response = new PageResponse<>(
                    List.of(), 0, 20, 0, 0);

            given(journeyService.getJourneys(any(), any(), any(), any(), any())).willReturn(response);

            mockMvc.perform(get("/api/v1/journeys"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("GET /journeys/{id} - 여정 상세 조회")
        void getJourneyDetail() throws Exception {
            JourneyDetailResponse responseDto = new JourneyDetailResponse(
                    100L, 5L, "John", "John VIP", JourneyStatus.IN_PROGRESS,
                    LocalDate.of(2026, 4, 15), LocalDate.of(2026, 4, 21),
                    "notes", List.of(), 45, LocalDateTime.now());

            given(journeyService.getJourneyDetail(eq(100L), any())).willReturn(responseDto);

            mockMvc.perform(get("/api/v1/journeys/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(100))
                    .andExpect(jsonPath("$.data.progress").value(45));
        }
    }

    @Nested
    @DisplayName("일정 항목 API")
    class ScheduleItemApiTest {

        @Test
        @DisplayName("POST /journeys/{id}/schedule-items - 일정 추가")
        void addScheduleItem() throws Exception {
            ScheduleItemCreateRequest request = new ScheduleItemCreateRequest(
                    LocalDateTime.of(2026, 4, 17, 14, 0),
                    "쇼핑 투어", ScheduleItemType.TOUR, "관광", 180,
                    new LocationDto("명동", "서울", 37.56, 126.99, null),
                    List.of("DRIVER"));

            ScheduleItemResponse responseDto = new ScheduleItemResponse(
                    510L, 100L, LocalDateTime.of(2026, 4, 17, 14, 0),
                    "쇼핑 투어", ScheduleItemType.TOUR, ScheduleItemStatus.SCHEDULED,
                    LocalDateTime.now(), LocalDateTime.now());

            given(journeyService.addScheduleItem(eq(100L), any())).willReturn(responseDto);

            mockMvc.perform(post("/api/v1/journeys/100/schedule-items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.title").value("쇼핑 투어"));
        }

        @Test
        @DisplayName("PUT /journeys/{id}/schedule-items/{itemId} - 일정 수정")
        void updateScheduleItem() throws Exception {
            ScheduleItemUpdateRequest request = new ScheduleItemUpdateRequest(
                    null, "수정된 제목", "변경 설명", 120, null, "사유");

            ScheduleItemResponse responseDto = new ScheduleItemResponse(
                    505L, 100L, LocalDateTime.of(2026, 4, 16, 11, 0),
                    "수정된 제목", ScheduleItemType.MEDICAL, ScheduleItemStatus.SCHEDULED,
                    LocalDateTime.now(), LocalDateTime.now());

            given(journeyService.updateScheduleItem(eq(100L), eq(505L), any())).willReturn(responseDto);

            mockMvc.perform(put("/api/v1/journeys/100/schedule-items/505")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("수정된 제목"));
        }

        @Test
        @DisplayName("DELETE /journeys/{id}/schedule-items/{itemId} - 일정 삭제")
        void deleteScheduleItem() throws Exception {
            doNothing().when(journeyService).deleteScheduleItem(100L, 505L);

            mockMvc.perform(delete("/api/v1/journeys/100/schedule-items/505"))
                    .andExpect(status().isNoContent());
        }
    }
}
