package com.k.medtour.domain.chat.dto;

import com.k.medtour.domain.chat.entity.ChatMessage;
import com.k.medtour.domain.chat.enums.ChatMessageType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ChatMessageResponse(
        String id,
        String roomId,
        Long senderId,
        String senderName,
        String senderRole,
        ChatMessageType type,
        String content,
        Map<String, String> translatedContent,
        LocalDateTime sentAt,
        List<Long> readBy
) {
    public static ChatMessageResponse from(ChatMessage message, List<Long> readBy) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoom().getRoomId(),
                message.getSenderId(),
                message.getSenderName(),
                message.getSenderRole(),
                message.getType(),
                message.getContent(),
                message.getTranslatedContent(),
                message.getSentAt(),
                readBy
        );
    }
}
