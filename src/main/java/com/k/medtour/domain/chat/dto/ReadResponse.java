package com.k.medtour.domain.chat.dto;

public record ReadResponse(
        String roomId,
        long unreadCount
) {
}
