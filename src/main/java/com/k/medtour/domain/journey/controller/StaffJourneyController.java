package com.k.medtour.domain.journey.controller;

import com.k.medtour.domain.journey.dto.NavigationResponse;
import com.k.medtour.domain.journey.dto.PatientNoticeResponse;
import com.k.medtour.domain.journey.dto.StaffTodayResponse;
import com.k.medtour.domain.journey.dto.StatusUpdateRequest;
import com.k.medtour.domain.journey.dto.StatusUpdateResponse;
import com.k.medtour.domain.journey.service.StaffAssignmentService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "실무자 여정", description = "실무자 당일 업무, 환자 특이사항, 상태 업데이트, 길찾기 API")
@RestController
@RequestMapping("/api/v1/journeys")
@RequiredArgsConstructor
public class StaffJourneyController {

    private final StaffAssignmentService staffAssignmentService;

    @Operation(summary = "당일 업무 리스트 조회", description = "실무자 본인의 당일(또는 지정일) 업무 목록 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/staff/me/today")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffTodayResponse> getTodayTasks(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success("조회 성공",
                staffAssignmentService.getTodayTasks(principal.memberId(), date));
    }

    @Operation(summary = "환자 특이사항 조회", description = "배정된 여정의 환자 특이사항(알레르기, 주의사항 등) 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여정을 찾을 수 없음")
    })
    @GetMapping("/{journeyId}/patient-notice")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<PatientNoticeResponse> getPatientNotice(
            @PathVariable Long journeyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("조회 성공",
                staffAssignmentService.getPatientNotice(journeyId, principal.memberId()));
    }

    @Operation(summary = "일정 상태 업데이트", description = "실무자가 일정 항목의 상태를 원터치로 업데이트")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 업데이트 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여정 또는 일정 항목을 찾을 수 없음")
    })
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

    @Operation(summary = "길찾기 딥링크 조회", description = "일정 항목 장소에 대한 지도 앱 딥링크 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여정 또는 일정 항목을 찾을 수 없음")
    })
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
