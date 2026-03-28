package com.k.medtour.domain.aftercare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceCreateRequest(
        @NotNull Long journeyId,
        @NotNull Long patientId,
        @NotBlank String currency,
        LocalDate dueDate,
        @NotNull List<InvoiceItemRequest> items
) {
    public record InvoiceItemRequest(
            @NotBlank String description,
            @NotNull BigDecimal unitPrice,
            @NotNull Integer quantity
    ) {
    }
}
