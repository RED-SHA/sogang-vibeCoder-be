package com.kmedical.domain.entity;

import com.kmedical.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * C24 — Invoice «entity»
 * 제약: ISSUED 상태 수정 불가; CANCELLED 후 재발행만 허용; 금액 USD BigDecimal
 */
public class Invoice {

    private String invoiceId;
    private String patientJourneyId;
    private String patientId;
    private String issuedBy;
    private BigDecimal totalAmountUSD;
    private InvoiceStatus status;
    private String pdfUrl;
    private LocalDateTime issuedAt;
    private LocalDateTime sentAt;

    public Invoice() {}

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getPatientJourneyId() { return patientJourneyId; }
    public void setPatientJourneyId(String patientJourneyId) { this.patientJourneyId = patientJourneyId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }

    public BigDecimal getTotalAmountUSD() { return totalAmountUSD; }
    public void setTotalAmountUSD(BigDecimal totalAmountUSD) { this.totalAmountUSD = totalAmountUSD; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
