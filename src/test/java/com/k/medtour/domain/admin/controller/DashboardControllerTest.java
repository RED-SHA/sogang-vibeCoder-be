package com.k.medtour.domain.admin.controller;

import com.k.medtour.domain.admin.dto.DashboardOverviewResponse;
import com.k.medtour.domain.admin.dto.StaffStatusResponse;
import com.k.medtour.domain.admin.service.DashboardService;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import com.k.medtour.support.SecurityTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

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
    @DisplayName("GET /api/v1/dashboard/overview")
    class GetOverviewTest {

        @Test
        @DisplayName("성공 - 대시보드 통계를 반환한다")
        void getOverview_success() throws Exception {
            DashboardOverviewResponse response = new DashboardOverviewResponse(10, 5, 3, 7);
            given(dashboardService.getOverview()).willReturn(response);

            mockMvc.perform(get("/api/v1/dashboard/overview"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalPatients").value(10))
                    .andExpect(jsonPath("$.data.activeJourneys").value(5))
                    .andExpect(jsonPath("$.data.todaySchedules").value(3))
                    .andExpect(jsonPath("$.data.unreadChats").value(7));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/dashboard/staff-status")
    class GetStaffStatusTest {

        @Test
        @DisplayName("성공 - 실무자 현황을 반환한다")
        void getStaffStatus_success() throws Exception {
            StaffStatusResponse response = new StaffStatusResponse(
                    3, 2, 1, 0,
                    List.of(
                            new StaffStatusResponse.StaffStatusItem(1L, "Kim", "DRIVER", "AVAILABLE"),
                            new StaffStatusResponse.StaffStatusItem(2L, "Lee", "INTERPRETER", "AVAILABLE"),
                            new StaffStatusResponse.StaffStatusItem(3L, "Park", "DRIVER", "ON_DUTY")
                    )
            );
            given(dashboardService.getStaffStatus()).willReturn(response);

            mockMvc.perform(get("/api/v1/dashboard/staff-status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalStaff").value(3))
                    .andExpect(jsonPath("$.data.available").value(2));
        }
    }
}
