package com.kmedical.dto.quotation;

/** Interface → QuotationController 간 견적 수락 요청 DTO */
public class QuotationAcceptRequestDTO {

    private String quotationId;
    private String patientId;

    public QuotationAcceptRequestDTO() {}

    public QuotationAcceptRequestDTO(String quotationId, String patientId) {
        this.quotationId = quotationId;
        this.patientId = patientId;
    }

    public String getQuotationId() { return quotationId; }
    public void setQuotationId(String quotationId) { this.quotationId = quotationId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
}
