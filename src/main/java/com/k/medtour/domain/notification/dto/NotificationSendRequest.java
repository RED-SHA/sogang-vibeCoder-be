package com.k.medtour.domain.notification.dto;

import com.k.medtour.domain.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NotificationSendRequest(
        @NotNull NotificationType type,
        @NotBlank String title,
        String content,
        Long referenceId,
        String referenceType,
        List<Long> memberIds,
        String targetRole
) {
}
