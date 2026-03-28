package com.k.medtour.domain.notification.service;

import com.k.medtour.domain.admin.repository.MemberRepository;
import com.k.medtour.domain.notification.dto.NotificationResponse;
import com.k.medtour.domain.notification.dto.NotificationSendRequest;
import com.k.medtour.domain.notification.dto.UnreadCountResponse;
import com.k.medtour.domain.notification.entity.Notification;
import com.k.medtour.domain.notification.enums.NotificationType;
import com.k.medtour.domain.notification.repository.NotificationRepository;
import com.k.medtour.global.common.PageResponse;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    public PageResponse<NotificationResponse> getMyNotifications(Long memberId, int page, int size) {
        Page<NotificationResponse> result = notificationRepository
                .findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(memberId, PageRequest.of(page, size))
                .map(NotificationResponse::from);
        return PageResponse.from(result);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.markAsRead();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsReadByMemberId(memberId);
    }

    public UnreadCountResponse getUnreadCount(Long memberId) {
        long count = notificationRepository.countByMemberIdAndIsReadFalseAndDeletedAtIsNull(memberId);
        return new UnreadCountResponse(count);
    }

    public PageResponse<NotificationResponse> getAdminAlerts(int page, int size) {
        List<NotificationType> alertTypes = List.of(NotificationType.SOS, NotificationType.SCHEDULE_CHANGE);
        Page<NotificationResponse> result = notificationRepository
                .findByTypeInOrderByCreatedAtDesc(alertTypes, PageRequest.of(page, size))
                .map(NotificationResponse::from);
        return PageResponse.from(result);
    }

    @Transactional
    public List<NotificationResponse> sendNotification(NotificationSendRequest request) {
        List<Long> targetMemberIds = new ArrayList<>();

        if (request.memberIds() != null && !request.memberIds().isEmpty()) {
            targetMemberIds.addAll(request.memberIds());
        }

        if (request.targetRole() != null && !request.targetRole().isBlank()) {
            memberRepository.findAllByRoleName(request.targetRole(), PageRequest.of(0, 1000))
                    .forEach(member -> targetMemberIds.add(member.getId()));
        }

        List<Notification> notifications = targetMemberIds.stream()
                .distinct()
                .map(memberId -> Notification.builder()
                        .memberId(memberId)
                        .type(request.type())
                        .title(request.title())
                        .content(request.content())
                        .referenceId(request.referenceId())
                        .referenceType(request.referenceType())
                        .build())
                .toList();

        List<Notification> saved = notificationRepository.saveAll(notifications);
        return saved.stream().map(NotificationResponse::from).toList();
    }

    @Transactional
    public Notification createNotification(Long memberId, NotificationType type,
                                           String title, String content,
                                           Long referenceId, String referenceType) {
        Notification notification = Notification.builder()
                .memberId(memberId)
                .type(type)
                .title(title)
                .content(content)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
        return notificationRepository.save(notification);
    }
}
