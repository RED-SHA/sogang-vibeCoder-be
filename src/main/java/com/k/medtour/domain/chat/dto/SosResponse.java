package com.k.medtour.domain.chat.dto;

import java.time.LocalDateTime;

public record SosResponse(
        String messageId,
        String roomId,
        String content,
        LocalDateTime sentAt
) {
}
