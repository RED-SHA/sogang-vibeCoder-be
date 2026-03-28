package com.k.medtour.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ReadRequest(
        @NotBlank(message = "마지막 읽은 메시지 ID는 필수입니다.")
        String lastReadMessageId
) {
}
