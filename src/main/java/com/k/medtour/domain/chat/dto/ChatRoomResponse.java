package com.k.medtour.domain.chat.dto;

import com.k.medtour.domain.chat.entity.ChatRoom;
import com.k.medtour.domain.chat.enums.ChatRoomType;

import java.time.LocalDateTime;
import java.util.List;

public record ChatRoomResponse(
        String roomId,
        ChatRoomType type,
        List<ParticipantResponse> participants,
        LocalDateTime createdAt
) {
    public record ParticipantResponse(
            Long userId,
            String name,
            String role
    ) {
    }

    public static ChatRoomResponse from(ChatRoom chatRoom, List<ParticipantResponse> participants) {
        return new ChatRoomResponse(
                chatRoom.getRoomId(),
                chatRoom.getType(),
                participants,
                chatRoom.getCreatedAt()
        );
    }
}
