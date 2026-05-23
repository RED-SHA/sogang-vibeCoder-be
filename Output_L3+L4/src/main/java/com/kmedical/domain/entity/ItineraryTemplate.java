package com.kmedical.domain.entity;

import java.time.LocalDateTime;

/** C13 — ItineraryTemplate «entity» */
public class ItineraryTemplate {

    private String itineraryTemplateId;
    private String agencyId;
    private String productType;
    private Integer durationDays;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ItineraryTemplate() {}

    public String getItineraryTemplateId() { return itineraryTemplateId; }
    public void setItineraryTemplateId(String itineraryTemplateId) { this.itineraryTemplateId = itineraryTemplateId; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
