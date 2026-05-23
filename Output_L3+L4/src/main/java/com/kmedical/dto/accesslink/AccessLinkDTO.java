package com.kmedical.dto.accesslink;

import com.kmedical.domain.enums.AccessLinkType;

import java.time.LocalDateTime;

/** AccessLink 도메인 복사 DTO — Interface 계층 노출용 */
public class AccessLinkDTO {

    private String accessLinkId;
    private String token;
    private AccessLinkType linkType;
    private String targetId;
    private String recipientUserId;
    private LocalDateTime expiresAt;
    private boolean isInvalidated;

    public AccessLinkDTO() {}

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

    public boolean isInvalidated() { return isInvalidated; }
    public void setInvalidated(boolean invalidated) { isInvalidated = invalidated; }
}
