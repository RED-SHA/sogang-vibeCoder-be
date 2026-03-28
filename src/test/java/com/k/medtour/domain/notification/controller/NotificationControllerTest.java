package com.k.medtour.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k.medtour.domain.notification.dto.NotificationResponse;
import com.k.medtour.domain.notification.dto.NotificationSendRequest;
import com.k.medtour.domain.notification.dto.UnreadCountResponse;
import com.k.medtour.domain.notification.enums.NotificationType;
import com.k.medtour.domain.notification.service.NotificationService;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import com.k.medtour.global.common.PageResponse;
import com.k.medtour.support.SecurityTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void tearDown() {
        SecurityTestUtil.clearAuthentication();
    }

    private NotificationResponse createResponse() {
        return new NotificationResponse(
                1L, 1L, NotificationType.SYSTEM,
                "Test", "Content", null, null,
                false, null, LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("GET /api/v1/notifications")
    class GetMyNotificationsTest {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "PATIENT");
        }

        @Test
        @DisplayName("성공 - 내 알림 목록을 반환한다")
        void getMyNotifications_success() throws Exception {
            PageResponse<NotificationResponse> response = new PageResponse<>(
                    List.of(createResponse()), 0, 20, 1, 1);
            given(notificationService.getMyNotifications(anyLong(), anyInt(), anyInt()))
                    .willReturn(response);

            mockMvc.perform(get("/api/v1/notifications")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/notifications/{id}/read")
    class MarkAsReadTest {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "PATIENT");
        }

        @Test
        @DisplayName("성공 - 알림 읽음 처리")
        void markAsRead_success() throws Exception {
            NotificationResponse response = createResponse();
            given(notificationService.markAsRead(anyLong(), anyLong())).willReturn(response);

            mockMvc.perform(post("/api/v1/notifications/1/read"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/notifications/read-all")
    class MarkAllAsReadTest {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "PATIENT");
        }

        @Test
        @DisplayName("성공 - 전체 읽음 처리")
        void markAllAsRead_success() throws Exception {
            mockMvc.perform(post("/api/v1/notifications/read-all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notifications/unread-count")
    class GetUnreadCountTest {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "PATIENT");
        }

        @Test
        @DisplayName("성공 - 미읽은 수 반환")
        void getUnreadCount_success() throws Exception {
            given(notificationService.getUnreadCount(anyLong()))
                    .willReturn(new UnreadCountResponse(5));

            mockMvc.perform(get("/api/v1/notifications/unread-count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.unreadCount").value(5));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notifications/admin/alerts")
    class GetAdminAlertsTest {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "ADMIN");
        }

        @Test
        @DisplayName("성공 - 긴급 알림 반환")
        void getAdminAlerts_success() throws Exception {
            PageResponse<NotificationResponse> response = new PageResponse<>(
                    List.of(createResponse()), 0, 20, 1, 1);
            given(notificationService.getAdminAlerts(anyInt(), anyInt()))
                    .willReturn(response);

            mockMvc.perform(get("/api/v1/notifications/admin/alerts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/notifications/send")
    class SendNotificationTest {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "ADMIN");
        }

        @Test
        @DisplayName("성공 - 알림 발송")
        void sendNotification_success() throws Exception {
            NotificationSendRequest request = new NotificationSendRequest(
                    NotificationType.SYSTEM, "Title", "Content",
                    null, null, List.of(1L), null
            );
            given(notificationService.sendNotification(any()))
                    .willReturn(List.of(createResponse()));

            mockMvc.perform(post("/api/v1/notifications/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
