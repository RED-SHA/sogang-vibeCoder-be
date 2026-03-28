package com.k.medtour.domain.notification.dto;

import com.k.medtour.domain.notification.entity.Notification;
import com.k.medtour.domain.notification.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long memberId,
        NotificationType type,
        String title,
        String content,
        Long referenceId,
        String referenceType,
        Boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getMemberId(),
                entity.getType(),
                entity.getTitle(),
                entity.getContent(),
                entity.getReferenceId(),
                entity.getReferenceType(),
                entity.getIsRead(),
                entity.getReadAt(),
                entity.getCreatedAt()
        );
    }
}
