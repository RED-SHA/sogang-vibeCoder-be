package com.k.medtour.domain.admin.controller;

import com.k.medtour.domain.admin.dto.*;
import com.k.medtour.domain.admin.service.MemberService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 실무자 본인 프로필 조회
     * GET /api/v1/members/staff/me
     */
    @GetMapping("/staff/me")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffProfileResponse> getStaffProfile(@AuthenticationPrincipal UserPrincipal principal) {
        StaffProfileResponse staffProfile = memberService.getStaffProfile(principal.memberId());
        return ApiResponse.success("조회 성공", staffProfile);
    }

    /**
     * 실무자 본인 프로필 수정
     * PUT /api/v1/members/staff/me
     */
    @PutMapping("/staff/me")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffProfileResponse> updateStaffProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody StaffProfileUpdateRequest request) {

        StaffProfileResponse staffProfile = memberService.updateStaffProfile(principal.memberId(), request);
        return ApiResponse.success("프로필 수정 완료", staffProfile);
    }

    /**
     * 에이전시 프로필 조회
     * GET /api/v1/members/agency
     */
    @GetMapping("/agency")
    public ApiResponse<AgencyProfileResponse> getAgencyProfile() {
        AgencyProfileResponse agencyProfile = memberService.getAgencyProfile();
        return ApiResponse.success("조회 성공", agencyProfile);
    }

    /**
     * 에이전시 프로필 수정
     * PUT /api/v1/members/agency
     */
    @PutMapping("/agency")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AgencyProfileResponse> updateAgencyProfile(@RequestBody AgencyProfileUpdateRequest request) {
        AgencyProfileResponse agencyProfile = memberService.updateAgencyProfile(request);
        return ApiResponse.success("수정 완료", agencyProfile);
    }

    /**
     * 에이전시 라이선스 검증
     * GET /api/v1/members/agency/license
     */
    @GetMapping("/agency/license")
    public ApiResponse<LicenseVerifyResponse> verifyLicense() {
        LicenseVerifyResponse licenseVerify = memberService.verifyLicense();
        return ApiResponse.success("조회 성공", licenseVerify);
    }
}
