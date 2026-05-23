package com.kmedical.dto.guide;

import java.time.LocalDateTime;

/** RecoveryGuide 도메인 복사 DTO — Interface 계층 노출용 */
public class RecoveryGuideDTO {

    private String recoveryGuideId;
    private String agencyId;
    private String surgeryType;
    private String titleEn;
    private String contentEn;
    private String pdfUrl;
    private LocalDateTime createdAt;

    public RecoveryGuideDTO() {}

    public String getRecoveryGuideId() { return recoveryGuideId; }
    public void setRecoveryGuideId(String recoveryGuideId) { this.recoveryGuideId = recoveryGuideId; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getSurgeryType() { return surgeryType; }
    public void setSurgeryType(String surgeryType) { this.surgeryType = surgeryType; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getContentEn() { return contentEn; }
    public void setContentEn(String contentEn) { this.contentEn = contentEn; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
