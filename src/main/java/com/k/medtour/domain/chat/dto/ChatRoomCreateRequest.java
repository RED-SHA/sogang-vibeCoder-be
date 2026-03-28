package com.k.medtour.domain.chat.dto;

import com.k.medtour.domain.chat.enums.ChatRoomType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatRoomCreateRequest(
        @NotNull(message = "채팅방 타입은 필수입니다.")
        ChatRoomType type,

        @NotNull(message = "참여자 목록은 필수입니다.")
        @Size(min = 2, message = "참여자는 최소 2명이어야 합니다.")
        List<ParticipantInfo> participants,

        Long journeyId,

        String language
) {
    public record ParticipantInfo(
            @NotNull Long userId,
            @NotNull String role
    ) {
    }
}
