package com.k.medtour.domain.proposal.dto;

import com.k.medtour.domain.proposal.entity.Proposal;
import com.k.medtour.domain.proposal.enums.ProposalStatus;

import java.time.LocalDateTime;

public record ProposalAcceptResponse(
        Long id,
        ProposalStatus status,
        LocalDateTime respondedAt
) {
    public static ProposalAcceptResponse from(Proposal entity) {
        return new ProposalAcceptResponse(
                entity.getId(),
                entity.getStatus(),
                entity.getRespondedAt()
        );
    }
}
