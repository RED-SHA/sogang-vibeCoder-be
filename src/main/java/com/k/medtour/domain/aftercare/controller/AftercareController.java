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

@RestController
@RequestMapping("/api/v1/aftercare")
@RequiredArgsConstructor
public class AftercareController {

    private final AftercareService aftercareService;

    /**
     * 사후 관리 가이드 생성 (ADMIN)
     */
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
    @GetMapping("/guides/{journeyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<AftercareGuideResponse> getGuide(@PathVariable Long journeyId) {
        return ApiResponse.success("조회 성공", aftercareService.getGuide(journeyId));
    }

    /**
     * 인보이스 생성 (ADMIN)
     */
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
    @GetMapping("/invoices/{journeyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT')")
    public ApiResponse<InvoiceResponse> getInvoice(@PathVariable Long journeyId) {
        return ApiResponse.success("조회 성공", aftercareService.getInvoiceByJourneyId(journeyId));
    }

    /**
     * 내 인보이스 목록 (PATIENT)
     */
    @GetMapping("/invoices/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<List<InvoiceResponse>> getMyInvoices(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("조회 성공", aftercareService.getMyInvoices(principal.memberId()));
    }

    /**
     * 업무 종료 리포트 (STAFF)
     */
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
