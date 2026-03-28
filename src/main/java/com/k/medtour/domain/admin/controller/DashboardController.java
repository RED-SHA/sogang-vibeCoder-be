package com.k.medtour.domain.admin.controller;

import com.k.medtour.domain.admin.dto.DashboardOverviewResponse;
import com.k.medtour.domain.admin.dto.StaffStatusResponse;
import com.k.medtour.domain.admin.service.DashboardService;
import com.k.medtour.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대시보드", description = "운영 현황 요약, 실무자 현황 등 관리자 대시보드 API")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 운영 현황 요약 (Overview)
     */
    @Operation(summary = "운영 현황 요약 조회", description = "금일 여정 수, 환자 수, 긴급 알림 등 운영 현황 요약 데이터 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<DashboardOverviewResponse> getOverview() {
        return ApiResponse.success("조회 성공", dashboardService.getOverview());
    }

    /**
     * 실무자 현황 (가용/업무중/오프라인)
     */
    @Operation(summary = "실무자 현황 조회", description = "실무자 상태별(가용/업무중/오프라인) 현황 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/staff-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<StaffStatusResponse> getStaffStatus() {
        return ApiResponse.success("조회 성공", dashboardService.getStaffStatus());
    }
}
