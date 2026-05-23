package com.kmedical.dto.alert;

import com.kmedical.domain.enums.AlertChannel;
import com.kmedical.domain.enums.AlertType;

/** Control → AlertController 간 알림 생성 요청 DTO */
public class AlertCreateRequestDTO {

    private AlertType alertType;
    private AlertChannel channel;
    private String recipientUserId;
    private String content;

    public AlertCreateRequestDTO() {}

    public AlertCreateRequestDTO(AlertType alertType, AlertChannel channel, String recipientUserId, String content) {
        this.alertType = alertType;
        this.channel = channel;
        this.recipientUserId = recipientUserId;
        this.content = content;
    }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public AlertChannel getChannel() { return channel; }
    public void setChannel(AlertChannel channel) { this.channel = channel; }

    public String getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(String recipientUserId) { this.recipientUserId = recipientUserId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
