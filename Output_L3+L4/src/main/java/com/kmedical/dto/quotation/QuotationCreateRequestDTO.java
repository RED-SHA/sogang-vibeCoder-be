package com.kmedical.dto.quotation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.kmedical.domain.enums.RequiredServiceType;

/** Interface → QuotationController 간 견적 요청 생성 DTO */
public class QuotationCreateRequestDTO {

    private String patientId;
    private LocalDate desiredVisitDate;
    private String surgeryType;
    private List<RequiredServiceType> requiredServices;
    private BigDecimal medicalFeeUSD;
    private BigDecimal conciergeFeeUSD;
    private String quotationRequestId;

    public QuotationCreateRequestDTO() {}

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public LocalDate getDesiredVisitDate() { return desiredVisitDate; }
    public void setDesiredVisitDate(LocalDate desiredVisitDate) { this.desiredVisitDate = desiredVisitDate; }

    public String getSurgeryType() { return surgeryType; }
    public void setSurgeryType(String surgeryType) { this.surgeryType = surgeryType; }

    public List<RequiredServiceType> getRequiredServices() { return requiredServices; }
    public void setRequiredServices(List<RequiredServiceType> requiredServices) { this.requiredServices = requiredServices; }

    public BigDecimal getMedicalFeeUSD() { return medicalFeeUSD; }
    public void setMedicalFeeUSD(BigDecimal medicalFeeUSD) { this.medicalFeeUSD = medicalFeeUSD; }

    public BigDecimal getConciergeFeeUSD() { return conciergeFeeUSD; }
    public void setConciergeFeeUSD(BigDecimal conciergeFeeUSD) { this.conciergeFeeUSD = conciergeFeeUSD; }

    public String getQuotationRequestId() { return quotationRequestId; }
    public void setQuotationRequestId(String quotationRequestId) { this.quotationRequestId = quotationRequestId; }
}
