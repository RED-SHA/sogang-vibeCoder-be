package com.k.medtour.domain.aftercare.entity;

import com.k.medtour.domain.aftercare.enums.InvoiceStatus;
import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice", indexes = {
        @Index(name = "idx_invoice_journey_id", columnList = "journey_id"),
        @Index(name = "idx_invoice_patient_id", columnList = "patient_id"),
        @Index(name = "idx_invoice_invoice_number", columnList = "invoice_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice extends BaseEntity {

    @Column(name = "journey_id", nullable = false)
    private Long journeyId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax", nullable = false, precision = 15, scale = 2)
    private BigDecimal tax;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @Builder
    public Invoice(Long journeyId, Long patientId, String invoiceNumber, String currency,
                   BigDecimal subtotal, BigDecimal tax, BigDecimal totalAmount,
                   LocalDate dueDate) {
        this.journeyId = journeyId;
        this.patientId = patientId;
        this.invoiceNumber = invoiceNumber;
        this.currency = currency;
        this.subtotal = subtotal;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.status = InvoiceStatus.DRAFT;
        this.issuedAt = LocalDateTime.now();
        this.dueDate = dueDate;
    }

    public void addItem(InvoiceItem item) {
        this.items.add(item);
    }

    public void send() {
        this.status = InvoiceStatus.SENT;
    }

    public void markAsPaid() {
        this.status = InvoiceStatus.PAID;
    }
}
