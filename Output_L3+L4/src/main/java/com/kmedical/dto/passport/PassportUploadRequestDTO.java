package com.kmedical.dto.passport;

/** Interface → PassportController 간 여권 업로드 요청 DTO */
public class PassportUploadRequestDTO {

    private String patientId;
    private String imageUrl;

    public PassportUploadRequestDTO() {}

    public PassportUploadRequestDTO(String patientId, String imageUrl) {
        this.patientId = patientId;
        this.imageUrl = imageUrl;
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
