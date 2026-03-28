package com.k.medtour.domain.proposal.controller;

import com.k.medtour.domain.proposal.dto.ProposalAcceptResponse;
import com.k.medtour.domain.proposal.dto.ProposalCreateRequest;
import com.k.medtour.domain.proposal.dto.ProposalListResponse;
import com.k.medtour.domain.proposal.dto.ProposalRejectResponse;
import com.k.medtour.domain.proposal.dto.ProposalRequestCreateRequest;
import com.k.medtour.domain.proposal.dto.ProposalRequestResponse;
import com.k.medtour.domain.proposal.dto.ProposalResponse;
import com.k.medtour.domain.proposal.dto.ProposalSendResponse;
import com.k.medtour.domain.proposal.enums.ProposalStatus;
import com.k.medtour.domain.proposal.service.ProposalService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "견적서", description = "견적서 생성, 조회, 발송, 수락/거절 및 견적 요청 API")
@RestController
@RequestMapping("/api/v1/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @Operation(summary = "견적서 생성", description = "관리자가 환자에게 보낼 견적서 생성")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "견적서 생성 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<ProposalResponse>> createProposal(
            @Valid @RequestBody ProposalCreateRequest request) {
        ProposalResponse response = proposalService.createProposal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("견적서 생성 완료", response));
    }

    @Operation(summary = "견적서 목록 조회", description = "관리자용 견적서 목록 조회 (상태, 환자 필터링)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<PageResponse<ProposalListResponse>>> getProposals(
            @RequestParam(required = false) ProposalStatus status,
            @RequestParam(required = false) Long patientId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<ProposalListResponse> response = proposalService.getProposals(status, patientId, pageable);
        return ResponseEntity.ok(ApiResponse.success("조회 성공", response));
    }

    @Operation(summary = "견적서 상세 조회", description = "특정 견적서의 상세 정보 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "견적서를 찾을 수 없음")
    })
    @GetMapping("/{proposalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER', 'PATIENT')")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposal(
            @Parameter(description = "견적서 ID", required = true) @PathVariable Long proposalId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProposalResponse response = proposalService.getProposal(
                proposalId, principal.memberId(), principal.role());
        return ResponseEntity.ok(ApiResponse.success("조회 성공", response));
    }

    @Operation(summary = "견적서 발송", description = "작성된 견적서를 환자에게 발송")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "견적서 발송 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "견적서를 찾을 수 없음")
    })
    @PostMapping("/{proposalId}/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<ProposalSendResponse>> sendProposal(
            @Parameter(description = "견적서 ID", required = true) @PathVariable Long proposalId) {
        ProposalSendResponse response = proposalService.sendProposal(proposalId);
        return ResponseEntity.ok(ApiResponse.success("견적서 발송 완료", response));
    }

    @Operation(summary = "견적 요청", description = "환자가 수술/컨시어지 견적을 요청")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "견적 요청 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/request")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<ProposalRequestResponse>> createProposalRequest(
            @Valid @RequestBody ProposalRequestCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProposalRequestResponse response = proposalService.createProposalRequest(
                principal.memberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("견적 요청 완료", response));
    }

    @Operation(summary = "내 견적서 목록 조회", description = "환자 본인에게 발송된 견적서 목록 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PageResponse<ProposalListResponse>>> getMyProposals(
            @RequestParam(required = false) ProposalStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        PageResponse<ProposalListResponse> response = proposalService.getMyProposals(
                principal.memberId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success("조회 성공", response));
    }

    @Operation(summary = "견적서 수락", description = "환자가 견적서를 수락")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "견적서 수락 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "견적서를 찾을 수 없음")
    })
    @PostMapping("/{proposalId}/accept")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<ProposalAcceptResponse>> acceptProposal(
            @Parameter(description = "견적서 ID", required = true) @PathVariable Long proposalId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProposalAcceptResponse response = proposalService.acceptProposal(
                proposalId, principal.memberId());
        return ResponseEntity.ok(ApiResponse.success("견적서 수락 완료", response));
    }

    @Operation(summary = "견적서 거절", description = "환자가 견적서를 거절")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "견적서 거절 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "견적서를 찾을 수 없음")
    })
    @PostMapping("/{proposalId}/reject")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<ProposalRejectResponse>> rejectProposal(
            @Parameter(description = "견적서 ID", required = true) @PathVariable Long proposalId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProposalRejectResponse response = proposalService.rejectProposal(
                proposalId, principal.memberId());
        return ResponseEntity.ok(ApiResponse.success("견적서 거절 완료", response));
    }
}
