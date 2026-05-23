package com.kmedical.dto.guide;

/** Interface → GuideController 간 가이드 배포 요청 DTO */
public class GuideDeliveryRequestDTO {

    private String recoveryGuideId;
    private String patientId;
    private String deliveredBy;

    public GuideDeliveryRequestDTO() {}

    public String getRecoveryGuideId() { return recoveryGuideId; }
    public void setRecoveryGuideId(String recoveryGuideId) { this.recoveryGuideId = recoveryGuideId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDeliveredBy() { return deliveredBy; }
    public void setDeliveredBy(String deliveredBy) { this.deliveredBy = deliveredBy; }
}
