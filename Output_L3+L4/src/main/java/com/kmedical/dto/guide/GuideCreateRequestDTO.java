package com.kmedical.dto.guide;

/** Interface → GuideController 간 회복 가이드 등록 요청 DTO */
public class GuideCreateRequestDTO {

    private String agencyId;
    private String surgeryType;
    private String titleEn;
    private String contentEn;
    private String pdfUrl;

    public GuideCreateRequestDTO() {}

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
}
