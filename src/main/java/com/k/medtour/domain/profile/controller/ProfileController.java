package com.k.medtour.domain.profile.controller;

import com.k.medtour.domain.profile.dto.PortfolioResponse;
import com.k.medtour.domain.profile.service.ProfileService;
import com.k.medtour.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "프로필", description = "병원 포트폴리오(Before & After) 조회 API")
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 포트폴리오 조회 (Before & After)
     */
    @Operation(summary = "포트폴리오 조회", description = "병원/기관의 시술 포트폴리오(Before & After) 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기관을 찾을 수 없음")
    })
    @GetMapping("/organizations/{id}/portfolio")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT')")
    public ApiResponse<PortfolioResponse> getPortfolio(@Parameter(description = "기관 ID", required = true) @PathVariable Long id) {
        return ApiResponse.success("조회 성공", profileService.getPortfolio(id));
    }
}
