package com.kmedical.dto.patient;

/** EmergencyContact 도메인 복사 DTO — Interface 계층 노출용 (환자당 최대 2건) */
public class EmergencyContactDTO {

    private String contactId;
    private String patientId;
    private String fullNameEn;
    private String relationship;
    private String phoneE164;
    private Integer sortOrder;

    public EmergencyContactDTO() {}

    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getFullNameEn() { return fullNameEn; }
    public void setFullNameEn(String fullNameEn) { this.fullNameEn = fullNameEn; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getPhoneE164() { return phoneE164; }
    public void setPhoneE164(String phoneE164) { this.phoneE164 = phoneE164; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
