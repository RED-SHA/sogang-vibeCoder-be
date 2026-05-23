package com.kmedical.dto.invoice;

import com.kmedical.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Invoice 도메인 복사 DTO — Interface 계층 노출용 */
public class InvoiceDTO {

    private String invoiceId;
    private String patientJourneyId;
    private String patientId;
    private String issuedBy;
    private BigDecimal totalAmountUSD;
    private InvoiceStatus status;
    private String pdfUrl;
    private LocalDateTime issuedAt;
    private LocalDateTime sentAt;

    public InvoiceDTO() {}

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
