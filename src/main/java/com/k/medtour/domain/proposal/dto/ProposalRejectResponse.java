package com.k.medtour.domain.proposal.dto;

import com.k.medtour.domain.proposal.entity.Proposal;
import com.k.medtour.domain.proposal.enums.ProposalStatus;

import java.time.LocalDateTime;

public record ProposalRejectResponse(
        Long id,
        ProposalStatus status,
        LocalDateTime respondedAt
) {
    public static ProposalRejectResponse from(Proposal entity) {
        return new ProposalRejectResponse(
                entity.getId(),
                entity.getStatus(),
                entity.getRespondedAt()
        );
    }
}
