package com.k.medtour.domain.admin.controller;

import com.k.medtour.domain.admin.dto.DashboardOverviewResponse;
import com.k.medtour.domain.admin.dto.StaffStatusResponse;
import com.k.medtour.domain.admin.service.DashboardService;
import com.k.medtour.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 운영 현황 요약 (Overview)
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<DashboardOverviewResponse> getOverview() {
        return ApiResponse.success("조회 성공", dashboardService.getOverview());
    }

    /**
     * 실무자 현황 (가용/업무중/오프라인)
     */
    @GetMapping("/staff-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<StaffStatusResponse> getStaffStatus() {
        return ApiResponse.success("조회 성공", dashboardService.getStaffStatus());
    }
}
