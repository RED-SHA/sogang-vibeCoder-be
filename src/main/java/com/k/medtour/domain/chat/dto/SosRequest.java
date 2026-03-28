package com.k.medtour.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record SosRequest(
        @NotBlank(message = "긴급 메시지 내용은 필수입니다.")
        String message,

        String roomId,

        Long journeyId
) {
}
