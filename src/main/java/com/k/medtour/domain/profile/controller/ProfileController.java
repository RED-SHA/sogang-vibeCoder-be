package com.k.medtour.domain.profile.controller;

import com.k.medtour.domain.profile.dto.PortfolioResponse;
import com.k.medtour.domain.profile.service.ProfileService;
import com.k.medtour.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 포트폴리오 조회 (Before & After)
     */
    @GetMapping("/organizations/{id}/portfolio")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT')")
    public ApiResponse<PortfolioResponse> getPortfolio(@PathVariable Long id) {
        return ApiResponse.success("조회 성공", profileService.getPortfolio(id));
    }
}
