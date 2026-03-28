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

@RestController
@RequestMapping("/api/v1/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<ProposalResponse>> createProposal(
            @Valid @RequestBody ProposalCreateRequest request) {
        ProposalResponse response = proposalService.createProposal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("견적서 생성 완료", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<PageResponse<ProposalListResponse>>> getProposals(
            @RequestParam(required = false) ProposalStatus status,
            @RequestParam(required = false) Long patientId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<ProposalListResponse> response = proposalService.getProposals(status, patientId, pageable);
        return ResponseEntity.ok(ApiResponse.success("조회 성공", response));
    }

    @GetMapping("/{proposalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER', 'PATIENT')")
    public ResponseEntity<ApiResponse<ProposalResponse>> getProposal(
            @PathVariable Long proposalId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProposalResponse response = proposalService.getProposal(
                proposalId, principal.memberId(), principal.role());
        return ResponseEntity.ok(ApiResponse.success("조회 성공", response));
    }

    @PostMapping("/{proposalId}/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<ApiResponse<ProposalSendResponse>> sendProposal(
            @PathVariable Long proposalId) {
        ProposalSendResponse response = proposalService.sendProposal(proposalId);
        return ResponseEntity.ok(ApiResponse.success("견적서 발송 완료", response));
    }

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

    @PostMapping("/{proposalId}/accept")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<ProposalAcceptResponse>> acceptProposal(
            @PathVariable Long proposalId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProposalAcceptResponse response = proposalService.acceptProposal(
                proposalId, principal.memberId());
        return ResponseEntity.ok(ApiResponse.success("견적서 수락 완료", response));
    }

    @PostMapping("/{proposalId}/reject")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<ProposalRejectResponse>> rejectProposal(
            @PathVariable Long proposalId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProposalRejectResponse response = proposalService.rejectProposal(
                proposalId, principal.memberId());
        return ResponseEntity.ok(ApiResponse.success("견적서 거절 완료", response));
    }
}
