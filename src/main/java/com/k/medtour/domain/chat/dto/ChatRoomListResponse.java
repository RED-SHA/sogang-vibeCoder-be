package com.k.medtour.domain.chat.dto;

import com.k.medtour.domain.chat.enums.ChatRoomType;

import java.time.LocalDateTime;

public record ChatRoomListResponse(
        String roomId,
        ChatRoomType type,
        OtherParticipant otherParticipant,
        LastMessageInfo lastMessage,
        long unreadCount,
        LocalDateTime updatedAt
) {
    public record OtherParticipant(
            Long userId,
            String name,
            String profileImageUrl
    ) {
    }

    public record LastMessageInfo(
            String content,
            LocalDateTime sentAt,
            boolean isTranslated
    ) {
    }
}
