package com.kmedical.dto.invoice;

import java.util.List;

/** Interface → InvoiceController 간 인보이스 생성 요청 DTO */
public class InvoiceCreateRequestDTO {

    private String patientJourneyId;
    private String patientId;
    private String issuedBy;
    private List<InvoiceItemDTO> items;

    public InvoiceCreateRequestDTO() {}

    public String getPatientJourneyId() { return patientJourneyId; }
    public void setPatientJourneyId(String patientJourneyId) { this.patientJourneyId = patientJourneyId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }

    public List<InvoiceItemDTO> getItems() { return items; }
    public void setItems(List<InvoiceItemDTO> items) { this.items = items; }
}
