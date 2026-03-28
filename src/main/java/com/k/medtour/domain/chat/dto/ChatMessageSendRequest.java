package com.k.medtour.domain.chat.dto;

import com.k.medtour.domain.chat.enums.ChatMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatMessageSendRequest(
        @NotNull(message = "메시지 타입은 필수입니다.")
        ChatMessageType type,

        @NotBlank(message = "메시지 내용은 필수입니다.")
        String content,

        String language
) {
}
