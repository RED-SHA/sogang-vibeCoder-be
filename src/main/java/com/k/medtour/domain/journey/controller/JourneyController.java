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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "여정", description = "여정 템플릿, 여정 생성/조회, 일정 항목 관리, 환자 타임라인 API")
@RestController
@RequestMapping("/api/v1/journeys")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyService journeyService;

    // ======================== Template Endpoints (ADMIN) ========================

    @Operation(summary = "여정 템플릿 목록 조회", description = "키워드, 카테고리로 필터링 가능한 템플릿 목록 페이징 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/templates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<PageResponse<TemplateListResponse>> getTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TemplateCategory category,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success("조회 성공", journeyService.getTemplates(keyword, category, pageable));
    }

    @Operation(summary = "여정 템플릿 생성", description = "새로운 여정 템플릿 생성")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "템플릿 생성 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/templates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody TemplateCreateRequest request) {
        TemplateResponse response = journeyService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("템플릿 생성 완료", response));
    }

    @Operation(summary = "여정 템플릿 상세 조회", description = "특정 여정 템플릿의 상세 정보 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    @GetMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<TemplateResponse> getTemplate(@Parameter(description = "템플릿 ID", required = true) @PathVariable Long templateId) {
        return ApiResponse.success("조회 성공", journeyService.getTemplate(templateId));
    }

    @Operation(summary = "여정 템플릿 수정", description = "기존 여정 템플릿 정보 수정")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "템플릿 수정 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    @PutMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<TemplateResponse> updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody TemplateCreateRequest request) {
        return ApiResponse.success("템플릿 수정 완료", journeyService.updateTemplate(templateId, request));
    }

    @Operation(summary = "여정 템플릿 삭제", description = "여정 템플릿 삭제")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    @DeleteMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<Void> deleteTemplate(@Parameter(description = "템플릿 ID", required = true) @PathVariable Long templateId) {
        journeyService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }

    // ======================== Journey Endpoints ========================

    @Operation(summary = "여정 생성", description = "새로운 환자 여정 생성")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "여정 생성 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<JourneyResponse>> createJourney(
            @Valid @RequestBody JourneyCreateRequest request) {
        JourneyResponse response = journeyService.createJourney(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("여정 생성 완료", response));
    }

    @Operation(summary = "여정 목록 조회", description = "상태, 환자, 날짜 필터링이 가능한 여정 목록 페이징 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
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

    @Operation(summary = "여정 상세 조회", description = "특정 여정의 상세 정보 및 일정 항목 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여정을 찾을 수 없음")
    })
    @GetMapping("/{journeyId}")
    public ApiResponse<JourneyDetailResponse> getJourneyDetail(
            @Parameter(description = "여정 ID", required = true) @PathVariable Long journeyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("조회 성공", journeyService.getJourneyDetail(journeyId, principal));
    }

    @Operation(summary = "실무자 배정", description = "여정에 실무자(기사/통역사) 배정")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "실무자 배정 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여정 또는 실무자를 찾을 수 없음")
    })
    @PostMapping("/{journeyId}/assign-staff")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<Void> assignStaff(
            @Parameter(description = "여정 ID", required = true) @PathVariable Long journeyId,
            @Valid @RequestBody StaffAssignRequest request) {
        journeyService.assignStaff(journeyId, request);
        return ApiResponse.success("실무자 배정 완료", null);
    }

    // ======================== Schedule Item Endpoints (ADMIN) ========================

    @Operation(summary = "일정 항목 추가", description = "여정에 새로운 일정 항목 추가")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "일정 추가 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여정을 찾을 수 없음")
    })
    @PostMapping("/{journeyId}/schedule-items")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<ApiResponse<ScheduleItemResponse>> addScheduleItem(
            @PathVariable Long journeyId,
            @Valid @RequestBody ScheduleItemCreateRequest request) {
        ScheduleItemResponse response = journeyService.addScheduleItem(journeyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("일정 추가 완료", response));
    }

    @Operation(summary = "일정 항목 수정", description = "여정의 특정 일정 항목 수정")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일정 수정 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여정 또는 일정 항목을 찾을 수 없음")
    })
    @PutMapping("/{journeyId}/schedule-items/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<ScheduleItemResponse> updateScheduleItem(
            @PathVariable Long journeyId,
            @PathVariable Long itemId,
            @Valid @RequestBody ScheduleItemUpdateRequest request) {
        return ApiResponse.success("일정 수정 완료",
                journeyService.updateScheduleItem(journeyId, itemId, request));
    }

    @Operation(summary = "일정 항목 삭제", description = "여정의 특정 일정 항목 삭제")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여정 또는 일정 항목을 찾을 수 없음")
    })
    @DeleteMapping("/{journeyId}/schedule-items/{itemId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<Void> deleteScheduleItem(
            @PathVariable Long journeyId,
            @PathVariable Long itemId) {
        journeyService.deleteScheduleItem(journeyId, itemId);
        return ResponseEntity.noContent().build();
    }

    // ======================== Patient Timeline ========================

    @Operation(summary = "환자 타임라인 조회", description = "환자 본인의 라이브 타임라인(일정표) 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/me/timeline")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<TimelineResponse> getTimeline(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success("조회 성공", journeyService.getTimeline(principal.memberId(), date));
    }
}
