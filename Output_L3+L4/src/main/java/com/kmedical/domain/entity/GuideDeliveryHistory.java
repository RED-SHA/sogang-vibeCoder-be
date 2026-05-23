package com.kmedical.domain.entity;

import java.time.LocalDateTime;

/** C23 — GuideDeliveryHistory «entity» */
public class GuideDeliveryHistory {

    private String guideDeliveryHistoryId;
    private String recoveryGuideId;
    private String patientId;
    private String deliveredBy;
    private LocalDateTime deliveredAt;
    private Boolean isVisible;

    public GuideDeliveryHistory() {}

    public String getGuideDeliveryHistoryId() { return guideDeliveryHistoryId; }
    public void setGuideDeliveryHistoryId(String guideDeliveryHistoryId) { this.guideDeliveryHistoryId = guideDeliveryHistoryId; }

    public String getRecoveryGuideId() { return recoveryGuideId; }
    public void setRecoveryGuideId(String recoveryGuideId) { this.recoveryGuideId = recoveryGuideId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDeliveredBy() { return deliveredBy; }
    public void setDeliveredBy(String deliveredBy) { this.deliveredBy = deliveredBy; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }
}
