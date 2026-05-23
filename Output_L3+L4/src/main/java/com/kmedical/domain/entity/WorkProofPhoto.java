package com.kmedical.domain.entity;

import java.time.LocalDateTime;

/**
 * C18 — WorkProofPhoto «entity»
 * 제약: retentionExpiresAt = Invoice.issuedAt + 1년 (Invoice ISSUED 시 자동 갱신)
 */
public class WorkProofPhoto {

    private String workProofPhotoId;
    private String assignmentId;
    private String staffId;
    private String fileUrl;
    private LocalDateTime takenAt;
    private LocalDateTime uploadedAt;
    private LocalDateTime retentionExpiresAt;

    public WorkProofPhoto() {}

    public String getWorkProofPhotoId() { return workProofPhotoId; }
    public void setWorkProofPhotoId(String workProofPhotoId) { this.workProofPhotoId = workProofPhotoId; }

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public LocalDateTime getRetentionExpiresAt() { return retentionExpiresAt; }
    public void setRetentionExpiresAt(LocalDateTime retentionExpiresAt) { this.retentionExpiresAt = retentionExpiresAt; }
}
