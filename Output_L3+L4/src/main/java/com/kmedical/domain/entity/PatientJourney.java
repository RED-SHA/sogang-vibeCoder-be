package com.kmedical.domain.entity;

import com.kmedical.domain.enums.JourneyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** C14 — PatientJourney «entity» */
public class PatientJourney {

    private String patientJourneyId;
    private String patientId;
    private String agencyId;
    private String quotationId;
    private String itineraryTemplateId;
    private JourneyStatus status;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PatientJourney() {}

    public String getPatientJourneyId() { return patientJourneyId; }
    public void setPatientJourneyId(String patientJourneyId) { this.patientJourneyId = patientJourneyId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getQuotationId() { return quotationId; }
    public void setQuotationId(String quotationId) { this.quotationId = quotationId; }

    public String getItineraryTemplateId() { return itineraryTemplateId; }
    public void setItineraryTemplateId(String itineraryTemplateId) { this.itineraryTemplateId = itineraryTemplateId; }

    public JourneyStatus getStatus() { return status; }
    public void setStatus(JourneyStatus status) { this.status = status; }

    public LocalDate getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }

    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
