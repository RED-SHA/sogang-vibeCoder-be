package com.kmedical.dto.accesslink;

import com.kmedical.domain.enums.AccessLinkType;

/** AccessLinkController → Interface 간 매직링크 검증 응답 DTO */
public class AccessLinkVerifyResponseDTO {

    private boolean valid;
    private AccessLinkType linkType;
    private String targetId;
    private String sessionToken;
    private String failureReason;

    public AccessLinkVerifyResponseDTO() {}

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public AccessLinkType getLinkType() { return linkType; }
    public void setLinkType(AccessLinkType linkType) { this.linkType = linkType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
