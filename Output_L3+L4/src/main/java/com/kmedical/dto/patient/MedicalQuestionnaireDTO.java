package com.kmedical.dto.patient;

import java.time.LocalDateTime;
import java.util.List;

/** MedicalQuestionnaire 도메인 복사 DTO — Interface 계층 노출용 */
public class MedicalQuestionnaireDTO {

    private String questionnaireId;
    private String patientId;
    private List<String> currentMedications;
    private List<String> allergies;
    private List<String> pastSurgeries;
    private String medicalNotes;
    private LocalDateTime submittedAt;

    public MedicalQuestionnaireDTO() {}

    public String getQuestionnaireId() { return questionnaireId; }
    public void setQuestionnaireId(String questionnaireId) { this.questionnaireId = questionnaireId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public List<String> getCurrentMedications() { return currentMedications; }
    public void setCurrentMedications(List<String> currentMedications) { this.currentMedications = currentMedications; }

    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }

    public List<String> getPastSurgeries() { return pastSurgeries; }
    public void setPastSurgeries(List<String> pastSurgeries) { this.pastSurgeries = pastSurgeries; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
