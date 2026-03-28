package com.k.medtour.domain.aftercare.dto;

import com.k.medtour.domain.aftercare.entity.Invoice;
import com.k.medtour.domain.aftercare.entity.InvoiceItem;
import com.k.medtour.domain.aftercare.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(
        Long id,
        Long journeyId,
        Long patientId,
        String invoiceNumber,
        String currency,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal totalAmount,
        InvoiceStatus status,
        LocalDateTime issuedAt,
        LocalDate dueDate,
        List<InvoiceItemResponse> items,
        LocalDateTime createdAt
) {
    public record InvoiceItemResponse(
            Long id,
            String description,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal amount
    ) {
        public static InvoiceItemResponse from(InvoiceItem entity) {
            return new InvoiceItemResponse(
                    entity.getId(),
                    entity.getDescription(),
                    entity.getUnitPrice(),
                    entity.getQuantity(),
                    entity.getAmount()
            );
        }
    }

    public static InvoiceResponse from(Invoice entity) {
        return new InvoiceResponse(
                entity.getId(),
                entity.getJourneyId(),
                entity.getPatientId(),
                entity.getInvoiceNumber(),
                entity.getCurrency(),
                entity.getSubtotal(),
                entity.getTax(),
                entity.getTotalAmount(),
                entity.getStatus(),
                entity.getIssuedAt(),
                entity.getDueDate(),
                entity.getItems().stream()
                        .map(InvoiceItemResponse::from)
                        .toList(),
                entity.getCreatedAt()
        );
    }
}
