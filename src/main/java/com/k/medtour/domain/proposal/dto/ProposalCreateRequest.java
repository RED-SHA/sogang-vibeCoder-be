package com.k.medtour.domain.proposal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProposalCreateRequest(
        @NotNull(message = "환자 ID는 필수입니다.")
        Long patientId,

        @NotBlank(message = "제목은 필수입니다.")
        String title,

        String currency,

        LocalDateTime validUntil,

        @NotEmpty(message = "항목은 1개 이상 필요합니다.")
        @Valid
        List<ProposalItemDto> items,

        BigDecimal discountRate,

        String notes
) {
}
