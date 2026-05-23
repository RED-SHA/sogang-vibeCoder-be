package com.kmedical.dto.quotation;

import com.kmedical.domain.enums.QuotationRequestStatus;
import com.kmedical.domain.enums.RequiredServiceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** QuotationRequest 도메인 복사 DTO — Interface 계층 노출용 */
public class QuotationRequestDTO {

    private String quotationRequestId;
    private String patientId;
    private LocalDate desiredVisitDate;
    private String surgeryType;
    private List<RequiredServiceType> requiredServices;
    private QuotationRequestStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public QuotationRequestDTO() {}

    public String getQuotationRequestId() { return quotationRequestId; }
    public void setQuotationRequestId(String quotationRequestId) { this.quotationRequestId = quotationRequestId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public LocalDate getDesiredVisitDate() { return desiredVisitDate; }
    public void setDesiredVisitDate(LocalDate desiredVisitDate) { this.desiredVisitDate = desiredVisitDate; }

    public String getSurgeryType() { return surgeryType; }
    public void setSurgeryType(String surgeryType) { this.surgeryType = surgeryType; }

    public List<RequiredServiceType> getRequiredServices() { return requiredServices; }
    public void setRequiredServices(List<RequiredServiceType> requiredServices) { this.requiredServices = requiredServices; }

    public QuotationRequestStatus getStatus() { return status; }
    public void setStatus(QuotationRequestStatus status) { this.status = status; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
