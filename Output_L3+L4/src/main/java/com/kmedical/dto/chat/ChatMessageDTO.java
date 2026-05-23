package com.kmedical.dto.chat;

import com.kmedical.domain.enums.Language;

import java.time.LocalDateTime;

/** ChatMessage 도메인 복사 DTO — Interface 계층 노출용 */
public class ChatMessageDTO {

    private String chatMessageId;
    private String conversationId;
    private String senderId;
    private String originalText;
    private Language originalLang;
    private String translatedText;
    private Language translatedLang;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    public ChatMessageDTO() {}

    public String getChatMessageId() { return chatMessageId; }
    public void setChatMessageId(String chatMessageId) { this.chatMessageId = chatMessageId; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public Language getOriginalLang() { return originalLang; }
    public void setOriginalLang(Language originalLang) { this.originalLang = originalLang; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public Language getTranslatedLang() { return translatedLang; }
    public void setTranslatedLang(Language translatedLang) { this.translatedLang = translatedLang; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
