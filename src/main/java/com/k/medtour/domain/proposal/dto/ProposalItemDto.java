package com.k.medtour.domain.proposal.dto;

import com.k.medtour.domain.proposal.entity.ProposalItem;
import com.k.medtour.domain.proposal.enums.ProposalItemCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProposalItemDto(
        Long id,

        @NotNull(message = "카테고리는 필수입니다.")
        ProposalItemCategory category,

        @NotBlank(message = "항목명은 필수입니다.")
        String name,

        String description,

        @NotNull(message = "단가는 필수입니다.")
        @Min(value = 0, message = "단가는 0 이상이어야 합니다.")
        BigDecimal unitPrice,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        Integer quantity,

        BigDecimal amount
) {
    public static ProposalItemDto from(ProposalItem entity) {
        return new ProposalItemDto(
                entity.getId(),
                entity.getCategory(),
                entity.getName(),
                entity.getDescription(),
                entity.getUnitPrice(),
                entity.getQuantity(),
                entity.getAmount()
        );
    }

    public ProposalItem toEntity() {
        return ProposalItem.builder()
                .category(category)
                .name(name)
                .description(description)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .build();
    }
}
