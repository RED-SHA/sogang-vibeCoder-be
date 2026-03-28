package com.k.medtour.domain.proposal.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProposalRequestCreateRequest(
        @NotEmpty(message = "희망 시술 목록은 필수입니다.")
        List<String> desiredProcedures,

        List<Long> preferredHospitalIds,

        @NotNull(message = "도착일은 필수입니다.")
        LocalDate arrivalDate,

        @NotNull(message = "출발일은 필수입니다.")
        LocalDate departureDate,

        String accommodationPreference,

        List<String> conciergeServices,

        BigDecimal budgetMin,
        BigDecimal budgetMax,
        String budgetCurrency,

        String additionalRequests
) {
}
