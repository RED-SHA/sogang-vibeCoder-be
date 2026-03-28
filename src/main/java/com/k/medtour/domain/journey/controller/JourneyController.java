package com.k.medtour.domain.journey.controller;

import com.k.medtour.domain.journey.dto.JourneyCreateRequest;
import com.k.medtour.domain.journey.dto.JourneyDetailResponse;
import com.k.medtour.domain.journey.dto.JourneyListResponse;
import com.k.medtour.domain.journey.dto.JourneyResponse;
import com.k.medtour.domain.journey.dto.ScheduleItemCreateRequest;
import com.k.medtour.domain.journey.dto.ScheduleItemResponse;
import com.k.medtour.domain.journey.dto.ScheduleItemUpdateRequest;
import com.k.medtour.domain.journey.dto.StaffAssignRequest;
import com.k.medtour.domain.journey.dto.TemplateCreateRequest;
import com.k.medtour.domain.journey.dto.TemplateListResponse;
import com.k.medtour.domain.journey.dto.TemplateResponse;
import com.k.medtour.domain.journey.dto.TimelineResponse;
import com.k.medtour.domain.journey.enums.JourneyStatus;
import com.k.medtour.domain.journey.enums.TemplateCategory;
import com.k.medtour.domain.journey.service.JourneyService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.ApiResponse;
import com.k.medtour.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/journeys")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyService journeyService;

    // ======================== Template Endpoints (ADMIN) ========================

    @GetMapping("/templates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<PageResponse<TemplateListResponse>> getTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TemplateCategory category,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success("조회 성공", journeyService.getTemplates(keyword, category, pageable));
    }

    @PostMapping("/templates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody TemplateCreateRequest request) {
        TemplateResponse response = journeyService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("템플릿 생성 완료", response));
    }

    @GetMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<TemplateResponse> getTemplate(@PathVariable Long templateId) {
        return ApiResponse.success("조회 성공", journeyService.getTemplate(templateId));
    }

    @PutMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<TemplateResponse> updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody TemplateCreateRequest request) {
        return ApiResponse.success("템플릿 수정 완료", journeyService.updateTemplate(templateId, request));
    }

    @DeleteMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long templateId) {
        journeyService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }

    // ======================== Journey Endpoints ========================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<JourneyResponse>> createJourney(
            @Valid @RequestBody JourneyCreateRequest request) {
        JourneyResponse response = journeyService.createJourney(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("여정 생성 완료", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<PageResponse<JourneyListResponse>> getJourneys(
            @RequestParam(required = false) JourneyStatus status,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success("조회 성공",
                journeyService.getJourneys(status, patientId, startDateFrom, startDateTo, pageable));
    }

    @GetMapping("/{journeyId}")
    public ApiResponse<JourneyDetailResponse> getJourneyDetail(
            @PathVariable Long journeyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("조회 성공", journeyService.getJourneyDetail(journeyId, principal));
    }

    @PostMapping("/{journeyId}/assign-staff")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<Void> assignStaff(
            @PathVariable Long journeyId,
            @Valid @RequestBody StaffAssignRequest request) {
        journeyService.assignStaff(journeyId, request);
        return ApiResponse.success("실무자 배정 완료", null);
    }

    // ======================== Schedule Item Endpoints (ADMIN) ========================

    @PostMapping("/{journeyId}/schedule-items")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<ScheduleItemResponse>> addScheduleItem(
            @PathVariable Long journeyId,
            @Valid @RequestBody ScheduleItemCreateRequest request) {
        ScheduleItemResponse response = journeyService.addScheduleItem(journeyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("일정 추가 완료", response));
    }

    @PutMapping("/{journeyId}/schedule-items/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<ScheduleItemResponse> updateScheduleItem(
            @PathVariable Long journeyId,
            @PathVariable Long itemId,
            @Valid @RequestBody ScheduleItemUpdateRequest request) {
        return ApiResponse.success("일정 수정 완료",
                journeyService.updateScheduleItem(journeyId, itemId, request));
    }

    @DeleteMapping("/{journeyId}/schedule-items/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<Void> deleteScheduleItem(
            @PathVariable Long journeyId,
            @PathVariable Long itemId) {
        journeyService.deleteScheduleItem(journeyId, itemId);
        return ResponseEntity.noContent().build();
    }

    // ======================== Patient Timeline ========================

    @GetMapping("/me/timeline")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<TimelineResponse> getTimeline(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success("조회 성공", journeyService.getTimeline(principal.memberId(), date));
    }
}
