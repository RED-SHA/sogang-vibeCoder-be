package com.k.medtour.domain.aftercare.controller;

import com.k.medtour.domain.aftercare.dto.AftercareGuideCreateRequest;
import com.k.medtour.domain.aftercare.dto.AftercareGuideResponse;
import com.k.medtour.domain.aftercare.dto.InvoiceCreateRequest;
import com.k.medtour.domain.aftercare.dto.InvoiceResponse;
import com.k.medtour.domain.aftercare.dto.StaffReportCreateRequest;
import com.k.medtour.domain.aftercare.dto.StaffReportResponse;
import com.k.medtour.domain.aftercare.service.AftercareService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "사후관리", description = "사후 관리 가이드, 인보이스, 업무 종료 리포트 API")
@RestController
@RequestMapping("/api/v1/aftercare")
@RequiredArgsConstructor
public class AftercareController {

    private final AftercareService aftercareService;

    /**
     * 사후 관리 가이드 생성 (ADMIN)
     */
    @Operation(summary = "사후 관리 가이드 생성", description = "관리자가 여정에 대한 사후 관리 가이드 생성")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "가이드 생성 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/guides")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<AftercareGuideResponse> createGuide(
            @Valid @RequestBody AftercareGuideCreateRequest request) {
        return ApiResponse.success("가이드 생성 완료", aftercareService.createGuide(request));
    }

    /**
     * 사후 관리 가이드 조회
     */
    @Operation(summary = "사후 관리 가이드 조회", description = "여정에 대한 사후 관리 가이드 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가이드를 찾을 수 없음")
    })
    @GetMapping("/guides/{journeyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<AftercareGuideResponse> getGuide(@Parameter(description = "여정 ID", required = true) @PathVariable Long journeyId) {
        return ApiResponse.success("조회 성공", aftercareService.getGuide(journeyId));
    }

    /**
     * 인보이스 생성 (ADMIN)
     */
    @Operation(summary = "인보이스 생성", description = "관리자가 여정에 대한 인보이스(정산서) 생성")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "인보이스 생성 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<InvoiceResponse> createInvoice(
            @Valid @RequestBody InvoiceCreateRequest request) {
        return ApiResponse.success("인보이스 생성 완료", aftercareService.createInvoice(request));
    }

    /**
     * 인보이스 조회 (여정별)
     */
    @Operation(summary = "인보이스 조회", description = "특정 여정에 대한 인보이스 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "인보이스를 찾을 수 없음")
    })
    @GetMapping("/invoices/{journeyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT')")
    public ApiResponse<InvoiceResponse> getInvoice(@Parameter(description = "여정 ID", required = true) @PathVariable Long journeyId) {
        return ApiResponse.success("조회 성공", aftercareService.getInvoiceByJourneyId(journeyId));
    }

    /**
     * 내 인보이스 목록 (PATIENT)
     */
    @Operation(summary = "내 인보이스 목록 조회", description = "환자 본인의 인보이스 목록 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/invoices/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<List<InvoiceResponse>> getMyInvoices(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("조회 성공", aftercareService.getMyInvoices(principal.memberId()));
    }

    /**
     * 업무 종료 리포트 (STAFF)
     */
    @Operation(summary = "업무 종료 리포트 작성", description = "실무자가 업무 종료 후 리포트 작성")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "리포트 생성 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffReportResponse> createReport(
            @Valid @RequestBody StaffReportCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("리포트 생성 완료",
                aftercareService.createReport(request, principal.memberId()));
    }
}
