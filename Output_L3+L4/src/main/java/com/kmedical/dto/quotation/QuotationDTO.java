package com.kmedical.dto.quotation;

import com.kmedical.domain.enums.QuotationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Quotation 도메인 복사 DTO — Interface 계층 노출용 */
public class QuotationDTO {

    private String quotationId;
    private String quotationRequestId;
    private String agencyId;
    private BigDecimal medicalFeeUSD;
    private BigDecimal conciergeFeeUSD;
    private BigDecimal totalFeeUSD;
    private QuotationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;

    public QuotationDTO() {}

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
