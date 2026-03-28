package com.k.medtour.domain.notification.service;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.admin.entity.Role;
import com.k.medtour.domain.admin.repository.MemberRepository;
import com.k.medtour.domain.notification.dto.NotificationResponse;
import com.k.medtour.domain.notification.dto.NotificationSendRequest;
import com.k.medtour.domain.notification.dto.UnreadCountResponse;
import com.k.medtour.domain.notification.entity.Notification;
import com.k.medtour.domain.notification.enums.NotificationType;
import com.k.medtour.domain.notification.repository.NotificationRepository;
import com.k.medtour.global.common.PageResponse;
import com.k.medtour.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MemberRepository memberRepository;

    private Notification createNotification(Long memberId, NotificationType type) {
        return Notification.builder()
                .memberId(memberId)
                .type(type)
                .title("Test Notification")
                .content("Test Content")
                .build();
    }

    @Nested
    @DisplayName("getMyNotifications")
    class GetMyNotificationsTest {

        @Test
        @DisplayName("성공 - 내 알림 목록을 반환한다")
        void getMyNotifications_success() {
            // Given
            Long memberId = 1L;
            Notification notification = createNotification(memberId, NotificationType.SYSTEM);
            Page<Notification> page = new PageImpl<>(List.of(notification));
            given(notificationRepository.findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                    eq(memberId), any(PageRequest.class))).willReturn(page);

            // When
            PageResponse<NotificationResponse> result = notificationService
                    .getMyNotifications(memberId, 0, 20);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).title()).isEqualTo("Test Notification");
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsReadTest {

        @Test
        @DisplayName("성공 - 알림을 읽음 처리한다")
        void markAsRead_success() {
            // Given
            Long memberId = 1L;
            Notification notification = createNotification(memberId, NotificationType.SYSTEM);
            given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

            // When
            NotificationResponse result = notificationService.markAsRead(1L, memberId);

            // Then
            assertThat(result.isRead()).isTrue();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 알림이면 예외 발생")
        void markAsRead_notFound() {
            // Given
            given(notificationRepository.findById(1L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> notificationService.markAsRead(1L, 1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("실패 - 본인의 알림이 아니면 예외 발생")
        void markAsRead_accessDenied() {
            // Given
            Notification notification = createNotification(2L, NotificationType.SYSTEM);
            given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

            // When & Then
            assertThatThrownBy(() -> notificationService.markAsRead(1L, 1L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("markAllAsRead")
    class MarkAllAsReadTest {

        @Test
        @DisplayName("성공 - 전체 읽음 처리")
        void markAllAsRead_success() {
            // Given
            Long memberId = 1L;
            given(notificationRepository.markAllAsReadByMemberId(memberId)).willReturn(5);

            // When
            notificationService.markAllAsRead(memberId);

            // Then
            verify(notificationRepository).markAllAsReadByMemberId(memberId);
        }
    }

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCountTest {

        @Test
        @DisplayName("성공 - 미읽은 알림 수를 반환한다")
        void getUnreadCount_success() {
            // Given
            Long memberId = 1L;
            given(notificationRepository.countByMemberIdAndIsReadFalseAndDeletedAtIsNull(memberId))
                    .willReturn(3L);

            // When
            UnreadCountResponse result = notificationService.getUnreadCount(memberId);

            // Then
            assertThat(result.unreadCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getAdminAlerts")
    class GetAdminAlertsTest {

        @Test
        @DisplayName("성공 - SOS/SCHEDULE_CHANGE 알림을 반환한다")
        void getAdminAlerts_success() {
            // Given
            Notification sosNotification = createNotification(1L, NotificationType.SOS);
            Page<Notification> page = new PageImpl<>(List.of(sosNotification));
            given(notificationRepository.findByTypeInOrderByCreatedAtDesc(
                    any(), any(PageRequest.class))).willReturn(page);

            // When
            PageResponse<NotificationResponse> result = notificationService.getAdminAlerts(0, 20);

            // Then
            assertThat(result.content()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("sendNotification")
    class SendNotificationTest {

        @Test
        @DisplayName("성공 - 특정 회원에게 알림을 발송한다")
        void sendNotification_toMembers() {
            // Given
            NotificationSendRequest request = new NotificationSendRequest(
                    NotificationType.SYSTEM,
                    "System Notice",
                    "Content",
                    null, null,
                    List.of(1L, 2L),
                    null
            );

            given(notificationRepository.saveAll(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            List<NotificationResponse> result = notificationService.sendNotification(request);

            // Then
            assertThat(result).hasSize(2);
        }
    }
}
