package com.k.medtour.domain.aftercare.entity;

import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_item", indexes = {
        @Index(name = "idx_invoice_item_invoice_id", columnList = "invoice_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvoiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Builder
    public InvoiceItem(Invoice invoice, String description, BigDecimal unitPrice,
                       Integer quantity, BigDecimal amount) {
        this.invoice = invoice;
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.amount = amount;
    }
}
