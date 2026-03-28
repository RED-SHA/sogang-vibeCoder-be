package com.k.medtour.domain.chat.dto;

import com.k.medtour.domain.chat.enums.ChatMessageType;

import java.time.LocalDateTime;

public record ChatFileMessageResponse(
        String id,
        String roomId,
        ChatMessageType type,
        FileInfo file,
        String caption,
        LocalDateTime sentAt
) {
    public record FileInfo(
            Long id,
            String fileName,
            Long fileSize,
            String mimeType,
            String url,
            boolean isSecure,
            LocalDateTime expiresAt
    ) {
    }
}
