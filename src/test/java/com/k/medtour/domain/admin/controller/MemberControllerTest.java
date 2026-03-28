package com.k.medtour.domain.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k.medtour.domain.admin.dto.*;
import com.k.medtour.domain.admin.service.MemberService;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("GET /api/v1/members/staff/me")
    class GetStaffProfileApiTest {

        @Test
        @DisplayName("성공 - 실무자 프로필이 조회된다")
        @WithMockUser(roles = "STAFF")
        void getStaffProfile_success() throws Exception {
            // Given
            StaffProfileResponse response = new StaffProfileResponse(
                    1L, "김기사", "driver@test.com", "+82-10-1234-5678",
                    "ROLE_STAFF", "DRIVER", List.of("ko", "en"), null,
                    Map.of("type", "SEDAN"), true, LocalDateTime.now()
            );
            given(memberService.getStaffProfile(any())).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/members/staff/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("김기사"))
                    .andExpect(jsonPath("$.data.staffType").value("DRIVER"));
        }

        @Test
        @DisplayName("실패 - 프로필이 없으면 404 반환")
        @WithMockUser(roles = "STAFF")
        void getStaffProfile_fail_notFound() throws Exception {
            // Given
            given(memberService.getStaffProfile(any()))
                    .willThrow(new BusinessException(ErrorCode.STAFF_PROFILE_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/members/staff/me"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/members/staff/me")
    class UpdateStaffProfileApiTest {

        @Test
        @DisplayName("성공 - 실무자 프로필이 수정된다")
        @WithMockUser(roles = "STAFF")
        void updateStaffProfile_success() throws Exception {
            // Given
            StaffProfileUpdateRequest request = new StaffProfileUpdateRequest(
                    "김기사 수정", "+82-10-9999-8888", List.of("ko", "en", "zh"), null,
                    Map.of("type", "SUV")
            );
            StaffProfileResponse response = new StaffProfileResponse(
                    1L, "김기사 수정", "driver@test.com", "+82-10-9999-8888",
                    "ROLE_STAFF", "DRIVER", List.of("ko", "en", "zh"), null,
                    Map.of("type", "SUV"), true, LocalDateTime.now()
            );
            given(memberService.updateStaffProfile(any(), any(StaffProfileUpdateRequest.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/members/staff/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("김기사 수정"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/agency")
    class GetAgencyProfileApiTest {

        @Test
        @DisplayName("성공 - 에이전시 프로필이 조회된다")
        @WithMockUser
        void getAgencyProfile_success() throws Exception {
            // Given
            AgencyProfileResponse response = new AgencyProfileResponse(
                    1L, "서울 뷰티 클리닉", "H-2025-001234", true,
                    "서울시 강남구", "+82-2-1234-5678", "https://example.com",
                    "성형외과 전문", LocalDateTime.now(), LocalDateTime.now()
            );
            given(memberService.getAgencyProfile()).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/members/agency"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("서울 뷰티 클리닉"));
        }

        @Test
        @DisplayName("실패 - 에이전시 프로필이 없으면 404 반환")
        @WithMockUser
        void getAgencyProfile_fail_notFound() throws Exception {
            // Given
            given(memberService.getAgencyProfile())
                    .willThrow(new BusinessException(ErrorCode.AGENCY_PROFILE_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/members/agency"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/agency/license")
    class LicenseVerifyApiTest {

        @Test
        @DisplayName("성공 - 라이선스 검증 정보가 조회된다")
        @WithMockUser
        void verifyLicense_success() throws Exception {
            // Given
            LicenseVerifyResponse response = new LicenseVerifyResponse(
                    1L, "서울 뷰티 클리닉", "H-2025-001234", true
            );
            given(memberService.verifyLicense()).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/members/agency/license"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.licenseVerified").value(true));
        }
    }
}
