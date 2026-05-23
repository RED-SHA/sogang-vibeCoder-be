package com.kmedical.domain.entity;

import com.kmedical.domain.enums.AccessLinkType;

import java.time.LocalDateTime;

/**
 * C26 — AccessLink «entity»
 * 제약: failedAttempts > 5 → 15분 잠금
 * 만료: PATIENT_GUEST_VIEW·PATIENT_PROPOSAL 72h, STAFF_INVITATION 24h, INVOICE_VIEW 30일
 */
public class AccessLink {

    private String accessLinkId;
    private String token;
    private AccessLinkType linkType;
    private String targetId;
    private String recipientUserId;
    private LocalDateTime expiresAt;
    private Boolean isInvalidated;
    private Integer failedAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;

    public AccessLink() {}

    public String getAccessLinkId() { return accessLinkId; }
    public void setAccessLinkId(String accessLinkId) { this.accessLinkId = accessLinkId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public AccessLinkType getLinkType() { return linkType; }
    public void setLinkType(AccessLinkType linkType) { this.linkType = linkType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(String recipientUserId) { this.recipientUserId = recipientUserId; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getIsInvalidated() { return isInvalidated; }
    public void setIsInvalidated(Boolean isInvalidated) { this.isInvalidated = isInvalidated; }

    public Integer getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(Integer failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
