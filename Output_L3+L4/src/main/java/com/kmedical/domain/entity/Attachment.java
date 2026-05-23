package com.kmedical.domain.entity;

import com.kmedical.domain.enums.AttachmentType;

import java.time.LocalDateTime;

/** C21 — Attachment «entity» */
public class Attachment {

    private String attachmentId;
    private String chatMessageId;
    private String fileUrl;
    private AttachmentType fileType;
    private Integer fileSizeBytes;
    private LocalDateTime uploadedAt;

    public Attachment() {}

    public String getAttachmentId() { return attachmentId; }
    public void setAttachmentId(String attachmentId) { this.attachmentId = attachmentId; }

    public String getChatMessageId() { return chatMessageId; }
    public void setChatMessageId(String chatMessageId) { this.chatMessageId = chatMessageId; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public AttachmentType getFileType() { return fileType; }
    public void setFileType(AttachmentType fileType) { this.fileType = fileType; }

    public Integer getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Integer fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
