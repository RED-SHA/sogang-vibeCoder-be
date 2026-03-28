package com.k.medtour.domain.journey.controller;

import com.k.medtour.domain.journey.dto.NavigationResponse;
import com.k.medtour.domain.journey.dto.PatientNoticeResponse;
import com.k.medtour.domain.journey.dto.StaffTodayResponse;
import com.k.medtour.domain.journey.dto.StatusUpdateRequest;
import com.k.medtour.domain.journey.dto.StatusUpdateResponse;
import com.k.medtour.domain.journey.service.StaffAssignmentService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/journeys")
@RequiredArgsConstructor
public class StaffJourneyController {

    private final StaffAssignmentService staffAssignmentService;

    @GetMapping("/staff/me/today")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffTodayResponse> getTodayTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success("조회 성공",
                staffAssignmentService.getTodayTasks(principal.memberId(), date));
    }

    @GetMapping("/{journeyId}/patient-notice")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<PatientNoticeResponse> getPatientNotice(
            @PathVariable Long journeyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("조회 성공",
                staffAssignmentService.getPatientNotice(journeyId, principal.memberId()));
    }

    @PatchMapping("/{journeyId}/schedule-items/{itemId}/status")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StatusUpdateResponse> updateStatus(
            @PathVariable Long journeyId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ApiResponse.success("상태 업데이트 완료",
                staffAssignmentService.updateStatus(journeyId, itemId, principal.memberId(), request));
    }

    @GetMapping("/{journeyId}/schedule-items/{itemId}/navigation")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<NavigationResponse> getNavigation(
            @PathVariable Long journeyId,
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "google") String platform) {
        return ApiResponse.success("조회 성공",
                staffAssignmentService.getNavigation(journeyId, itemId, platform));
    }
}
