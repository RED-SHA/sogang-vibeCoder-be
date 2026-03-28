package com.k.medtour.domain.proposal.dto;

import com.k.medtour.domain.proposal.entity.Proposal;
import com.k.medtour.domain.proposal.enums.ProposalStatus;

import java.time.LocalDateTime;

public record ProposalSendResponse(
        Long id,
        ProposalStatus status,
        LocalDateTime sentAt
) {
    public static ProposalSendResponse from(Proposal entity) {
        return new ProposalSendResponse(
                entity.getId(),
                entity.getStatus(),
                entity.getSentAt()
        );
    }
}
