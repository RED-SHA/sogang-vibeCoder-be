package com.kmedical.dto.accesslink;

import com.kmedical.domain.enums.AccessLinkType;

/** Interface → AccessLinkController 간 접속 링크 생성 요청 DTO */
public class AccessLinkCreateRequestDTO {

    private AccessLinkType linkType;
    private String targetId;
    private String recipientUserId;

    public AccessLinkCreateRequestDTO() {}

    public AccessLinkType getLinkType() { return linkType; }
    public void setLinkType(AccessLinkType linkType) { this.linkType = linkType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(String recipientUserId) { this.recipientUserId = recipientUserId; }
}
