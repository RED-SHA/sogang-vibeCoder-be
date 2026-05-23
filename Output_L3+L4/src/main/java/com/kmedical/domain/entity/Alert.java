package com.kmedical.domain.entity;

import com.kmedical.domain.enums.AlertChannel;
import com.kmedical.domain.enums.AlertStatus;
import com.kmedical.domain.enums.AlertType;

import java.time.LocalDateTime;

/** C27 — Alert «entity» */
public class Alert {

    private String alertId;
    private AlertType alertType;
    private AlertChannel channel;
    private String recipientUserId;
    private String content;
    private AlertStatus status;
    private LocalDateTime sentAt;
    private String failureReason;

    public Alert() {}

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public AlertChannel getChannel() { return channel; }
    public void setChannel(AlertChannel channel) { this.channel = channel; }

    public String getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(String recipientUserId) { this.recipientUserId = recipientUserId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
