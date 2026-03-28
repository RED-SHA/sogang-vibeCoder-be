package com.k.medtour.domain.proposal.dto;

import com.k.medtour.domain.proposal.entity.Proposal;
import com.k.medtour.domain.proposal.enums.ProposalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProposalResponse(
        Long id,
        Long patientId,
        String title,
        ProposalStatus status,
        String currency,
        List<ProposalItemDto> items,
        BigDecimal subtotal,
        BigDecimal discountRate,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        LocalDateTime validUntil,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        LocalDateTime respondedAt
) {
    public static ProposalResponse from(Proposal entity) {
        List<ProposalItemDto> itemDtos = entity.getItems().stream()
                .map(ProposalItemDto::from)
                .toList();

        return new ProposalResponse(
                entity.getId(),
                entity.getPatientId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getCurrency(),
                itemDtos,
                entity.getSubtotal(),
                entity.getDiscountRate(),
                entity.getDiscountAmount(),
                entity.getTotalAmount(),
                entity.getValidUntil(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getSentAt(),
                entity.getRespondedAt()
        );
    }
}
