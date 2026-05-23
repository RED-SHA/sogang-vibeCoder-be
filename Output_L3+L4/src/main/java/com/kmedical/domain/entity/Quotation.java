package com.kmedical.domain.entity;

import com.kmedical.domain.enums.QuotationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * C12 — Quotation «entity»
 * 제약: 동일 QuotationRequest에 ACCEPTED 상태 1건만 허용; 금액 USD 기준 BigDecimal
 */
public class Quotation {

    private String quotationId;
    private String quotationRequestId;
    private String agencyId;
    private BigDecimal medicalFeeUSD;
    private BigDecimal conciergeFeeUSD;
    private BigDecimal totalFeeUSD;
    private QuotationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;

    public Quotation() {}

    public String getQuotationId() { return quotationId; }
    public void setQuotationId(String quotationId) { this.quotationId = quotationId; }

    public String getQuotationRequestId() { return quotationRequestId; }
    public void setQuotationRequestId(String quotationRequestId) { this.quotationRequestId = quotationRequestId; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public BigDecimal getMedicalFeeUSD() { return medicalFeeUSD; }
    public void setMedicalFeeUSD(BigDecimal medicalFeeUSD) { this.medicalFeeUSD = medicalFeeUSD; }

    public BigDecimal getConciergeFeeUSD() { return conciergeFeeUSD; }
    public void setConciergeFeeUSD(BigDecimal conciergeFeeUSD) { this.conciergeFeeUSD = conciergeFeeUSD; }

    public BigDecimal getTotalFeeUSD() { return totalFeeUSD; }
    public void setTotalFeeUSD(BigDecimal totalFeeUSD) { this.totalFeeUSD = totalFeeUSD; }

    public QuotationStatus getStatus() { return status; }
    public void setStatus(QuotationStatus status) { this.status = status; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
}
