package com.k.medtour.domain.notification.controller;

import com.k.medtour.domain.notification.dto.NotificationResponse;
import com.k.medtour.domain.notification.dto.NotificationSendRequest;
import com.k.medtour.domain.notification.dto.UnreadCountResponse;
import com.k.medtour.domain.notification.service.NotificationService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.ApiResponse;
import com.k.medtour.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 내 알림 목록 조회 (페이징)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<PageResponse<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("조회 성공",
                notificationService.getMyNotifications(principal.memberId(), page, size));
    }

    /**
     * 알림 읽음 처리
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("읽음 처리 완료",
                notificationService.markAsRead(id, principal.memberId()));
    }

    /**
     * 전체 읽음 처리
     */
    @PostMapping("/read-all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.memberId());
        return ApiResponse.success("전체 읽음 처리 완료", null);
    }

    /**
     * 미읽은 알림 수
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("조회 성공",
                notificationService.getUnreadCount(principal.memberId()));
    }

    /**
     * 긴급 알림 조회 (관리자, SOS/SCHEDULE_CHANGE만)
     */
    @GetMapping("/admin/alerts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<PageResponse<NotificationResponse>> getAdminAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("조회 성공",
                notificationService.getAdminAlerts(page, size));
    }

    /**
     * 알림 발송 (관리자, 특정 회원/역할 대상)
     */
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<List<NotificationResponse>> sendNotification(
            @Valid @RequestBody NotificationSendRequest request) {
        return ApiResponse.success("알림 발송 완료",
                notificationService.sendNotification(request));
    }
}
