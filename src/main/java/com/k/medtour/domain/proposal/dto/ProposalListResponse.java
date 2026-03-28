package com.k.medtour.domain.proposal.dto;

import com.k.medtour.domain.proposal.entity.Proposal;
import com.k.medtour.domain.proposal.enums.ProposalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProposalListResponse(
        Long id,
        Long patientId,
        String title,
        ProposalStatus status,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime validUntil,
        LocalDateTime createdAt
) {
    public static ProposalListResponse from(Proposal entity) {
        return new ProposalListResponse(
                entity.getId(),
                entity.getPatientId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getValidUntil(),
                entity.getCreatedAt()
        );
    }
}
