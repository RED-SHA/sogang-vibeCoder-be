package com.kmedical.domain.entity;

import java.time.LocalDateTime;
import java.util.List;

/** C07 — Agency «entity» */
public class Agency {

    private String agencyId;
    private String nameKo;
    private String nameEn;
    private String licenseNumber;
    private String licenseDocumentUrl;
    private Boolean isVerified;
    private List<String> portfolioItems;
    private String contactEmail;
    private LocalDateTime updatedAt;

    public Agency() {}

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getNameKo() { return nameKo; }
    public void setNameKo(String nameKo) { this.nameKo = nameKo; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getLicenseDocumentUrl() { return licenseDocumentUrl; }
    public void setLicenseDocumentUrl(String licenseDocumentUrl) { this.licenseDocumentUrl = licenseDocumentUrl; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public List<String> getPortfolioItems() { return portfolioItems; }
    public void setPortfolioItems(List<String> portfolioItems) { this.portfolioItems = portfolioItems; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
