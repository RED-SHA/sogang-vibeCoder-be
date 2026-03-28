package com.k.medtour.domain.admin.controller;

import com.k.medtour.domain.admin.dto.*;
import com.k.medtour.domain.admin.service.MemberService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "실무자 프로필, 에이전시 프로필, 라이선스 검증 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 실무자 본인 프로필 조회
     * GET /api/v1/members/staff/me
     */
    @Operation(summary = "실무자 본인 프로필 조회", description = "로그인한 실무자의 프로필 정보 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
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
    @Operation(summary = "실무자 본인 프로필 수정", description = "로그인한 실무자의 프로필 정보 수정")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
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
    @Operation(summary = "에이전시 프로필 조회", description = "에이전시 기본 프로필 정보 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/agency")
    public ApiResponse<AgencyProfileResponse> getAgencyProfile() {
        AgencyProfileResponse agencyProfile = memberService.getAgencyProfile();
        return ApiResponse.success("조회 성공", agencyProfile);
    }

    /**
     * 에이전시 프로필 수정
     * PUT /api/v1/members/agency
     */
    @Operation(summary = "에이전시 프로필 수정", description = "에이전시 프로필 정보 수정 (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
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
    @Operation(summary = "에이전시 라이선스 검증", description = "에이전시의 사업자 라이선스 유효성 검증")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/agency/license")
    public ApiResponse<LicenseVerifyResponse> verifyLicense() {
        LicenseVerifyResponse licenseVerify = memberService.verifyLicense();
        return ApiResponse.success("조회 성공", licenseVerify);
    }
}
