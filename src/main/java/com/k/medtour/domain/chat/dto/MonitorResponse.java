package com.k.medtour.domain.chat.dto;

import com.k.medtour.domain.chat.enums.ChatRoomType;

import java.time.LocalDateTime;
import java.util.List;

public record MonitorResponse(
        long totalRooms,
        long activeRooms,
        long totalUnreadMessages,
        List<MonitorRoomInfo> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record MonitorRoomInfo(
            String roomId,
            ChatRoomType type,
            PatientInfo patient,
            Long journeyId,
            MonitorLastMessage lastMessage,
            long unreadCount,
            boolean isUrgent
    ) {
    }

    public record PatientInfo(
            Long id,
            String name
    ) {
    }

    public record MonitorLastMessage(
            String content,
            String translatedContent,
            LocalDateTime sentAt
    ) {
    }
}
