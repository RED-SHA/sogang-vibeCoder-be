package com.k.medtour.domain.proposal.service;

import com.k.medtour.domain.admin.repository.MemberRepository;
import com.k.medtour.domain.proposal.dto.ProposalAcceptResponse;
import com.k.medtour.domain.proposal.dto.ProposalCreateRequest;
import com.k.medtour.domain.proposal.dto.ProposalItemDto;
import com.k.medtour.domain.proposal.dto.ProposalListResponse;
import com.k.medtour.domain.proposal.dto.ProposalRejectResponse;
import com.k.medtour.domain.proposal.dto.ProposalRequestCreateRequest;
import com.k.medtour.domain.proposal.dto.ProposalRequestResponse;
import com.k.medtour.domain.proposal.dto.ProposalResponse;
import com.k.medtour.domain.proposal.dto.ProposalSendResponse;
import com.k.medtour.domain.proposal.entity.Proposal;
import com.k.medtour.domain.proposal.entity.ProposalItem;
import com.k.medtour.domain.proposal.entity.ProposalRequest;
import com.k.medtour.domain.proposal.enums.ProposalStatus;
import com.k.medtour.domain.proposal.repository.ProposalRepository;
import com.k.medtour.domain.proposal.repository.ProposalRequestRepository;
import com.k.medtour.global.common.PageResponse;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProposalRequestRepository proposalRequestRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ProposalResponse createProposal(ProposalCreateRequest request) {
        memberRepository.findById(request.patientId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Proposal proposal = Proposal.builder()
                .patientId(request.patientId())
                .title(request.title())
                .currency(request.currency() != null ? request.currency() : "USD")
                .discountRate(request.discountRate())
                .validUntil(request.validUntil())
                .notes(request.notes())
                .build();

        for (ProposalItemDto itemDto : request.items()) {
            ProposalItem item = itemDto.toEntity();
            proposal.addItem(item);
        }

        proposal.calculateAmounts();
        Proposal saved = proposalRepository.save(proposal);
        return ProposalResponse.from(saved);
    }

    public PageResponse<ProposalListResponse> getProposals(ProposalStatus status, Long patientId,
                                                            Pageable pageable) {
        Page<ProposalListResponse> page = proposalRepository
                .findAllWithFilters(status, patientId, pageable)
                .map(ProposalListResponse::from);
        return PageResponse.from(page);
    }

    public ProposalResponse getProposal(Long proposalId, Long memberId, String role) {
        Proposal proposal = findProposalOrThrow(proposalId);
        validateAccess(proposal, memberId, role);
        return ProposalResponse.from(proposal);
    }

    @Transactional
    public ProposalSendResponse sendProposal(Long proposalId) {
        Proposal proposal = findProposalOrThrow(proposalId);
        proposal.send();
        return ProposalSendResponse.from(proposal);
    }

    @Transactional
    public ProposalRequestResponse createProposalRequest(Long patientId,
                                                          ProposalRequestCreateRequest request) {
        ProposalRequest proposalRequest = ProposalRequest.builder()
                .patientId(patientId)
                .desiredProcedures(request.desiredProcedures())
                .preferredHospitalIds(request.preferredHospitalIds())
                .arrivalDate(request.arrivalDate())
                .departureDate(request.departureDate())
                .accommodationPreference(request.accommodationPreference())
                .conciergeServices(request.conciergeServices())
                .budgetMin(request.budgetMin())
                .budgetMax(request.budgetMax())
                .budgetCurrency(request.budgetCurrency())
                .additionalRequests(request.additionalRequests())
                .build();

        ProposalRequest saved = proposalRequestRepository.save(proposalRequest);
        return ProposalRequestResponse.from(saved);
    }

    public PageResponse<ProposalListResponse> getMyProposals(Long patientId, ProposalStatus status,
                                                              Pageable pageable) {
        Page<ProposalListResponse> page = proposalRepository
                .findByPatientIdWithFilter(patientId, status, pageable)
                .map(ProposalListResponse::from);
        return PageResponse.from(page);
    }

    @Transactional
    public ProposalAcceptResponse acceptProposal(Long proposalId, Long patientId) {
        Proposal proposal = findProposalOrThrow(proposalId);
        validatePatientAccess(proposal, patientId);
        validateRespondable(proposal);
        proposal.accept();
        return ProposalAcceptResponse.from(proposal);
    }

    @Transactional
    public ProposalRejectResponse rejectProposal(Long proposalId, Long patientId) {
        Proposal proposal = findProposalOrThrow(proposalId);
        validatePatientAccess(proposal, patientId);
        validateRespondable(proposal);
        proposal.reject();
        return ProposalRejectResponse.from(proposal);
    }

    private Proposal findProposalOrThrow(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROPOSAL_NOT_FOUND));
    }

    private void validateAccess(Proposal proposal, Long memberId, String role) {
        if ("ADMIN".equals(role) || "MASTER".equals(role)) {
            return;
        }
        if (!proposal.getPatientId().equals(memberId)) {
            throw new BusinessException(ErrorCode.PROPOSAL_ACCESS_DENIED);
        }
    }

    private void validatePatientAccess(Proposal proposal, Long patientId) {
        if (!proposal.getPatientId().equals(patientId)) {
            throw new BusinessException(ErrorCode.PROPOSAL_ACCESS_DENIED);
        }
    }

    private void validateRespondable(Proposal proposal) {
        if (!proposal.isRespondable()) {
            throw new BusinessException(ErrorCode.PROPOSAL_ALREADY_RESPONDED,
                    "이미 응답했거나 SENT 상태가 아닌 견적서입니다.");
        }
    }
}
