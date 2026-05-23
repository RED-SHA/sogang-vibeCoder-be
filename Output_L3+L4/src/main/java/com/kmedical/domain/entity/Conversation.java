package com.kmedical.domain.entity;

import com.kmedical.domain.enums.ConversationType;

import java.time.LocalDateTime;
import java.util.List;

/** C19 — Conversation «entity» */
public class Conversation {

    private String conversationId;
    private String patientJourneyId;
    private ConversationType conversationType;
    private List<String> participantIds;
    private String assignedCoordinatorId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    public Conversation() {}

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getPatientJourneyId() { return patientJourneyId; }
    public void setPatientJourneyId(String patientJourneyId) { this.patientJourneyId = patientJourneyId; }

    public ConversationType getConversationType() { return conversationType; }
    public void setConversationType(ConversationType conversationType) { this.conversationType = conversationType; }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    public String getAssignedCoordinatorId() { return assignedCoordinatorId; }
    public void setAssignedCoordinatorId(String assignedCoordinatorId) { this.assignedCoordinatorId = assignedCoordinatorId; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
