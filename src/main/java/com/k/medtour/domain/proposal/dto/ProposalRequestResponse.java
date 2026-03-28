package com.k.medtour.domain.proposal.dto;

import com.k.medtour.domain.proposal.entity.ProposalRequest;
import com.k.medtour.domain.proposal.enums.ProposalRequestStatus;

import java.time.LocalDateTime;

public record ProposalRequestResponse(
        Long requestId,
        ProposalRequestStatus status,
        LocalDateTime createdAt
) {
    public static ProposalRequestResponse from(ProposalRequest entity) {
        return new ProposalRequestResponse(
                entity.getId(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
