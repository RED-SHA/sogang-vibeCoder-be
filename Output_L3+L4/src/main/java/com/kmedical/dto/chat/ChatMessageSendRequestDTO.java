package com.kmedical.dto.chat;

import com.kmedical.domain.enums.Language;

/** Interface → ChatController 간 채팅 메시지 발송 요청 DTO */
public class ChatMessageSendRequestDTO {

    private String conversationId;
    private String senderId;
    private String originalText;
    private Language originalLang;

    public ChatMessageSendRequestDTO() {}

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public Language getOriginalLang() { return originalLang; }
    public void setOriginalLang(Language originalLang) { this.originalLang = originalLang; }
}
