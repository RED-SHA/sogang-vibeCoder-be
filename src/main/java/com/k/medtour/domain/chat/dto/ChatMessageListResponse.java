package com.k.medtour.domain.chat.dto;

import java.util.List;

public record ChatMessageListResponse(
        List<ChatMessageResponse> messages,
        String nextCursor,
        boolean hasMore
) {
}
