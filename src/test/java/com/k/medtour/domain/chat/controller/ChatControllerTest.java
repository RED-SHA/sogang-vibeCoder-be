package com.k.medtour.domain.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k.medtour.domain.chat.dto.ChatFileMessageResponse;
import com.k.medtour.domain.chat.dto.ChatMessageListResponse;
import com.k.medtour.domain.chat.dto.ChatMessageResponse;
import com.k.medtour.domain.chat.dto.ChatMessageSendRequest;
import com.k.medtour.domain.chat.dto.ChatRoomCreateRequest;
import com.k.medtour.domain.chat.dto.ChatRoomListResponse;
import com.k.medtour.domain.chat.dto.ChatRoomResponse;
import com.k.medtour.domain.chat.dto.MonitorResponse;
import com.k.medtour.domain.chat.dto.ReadRequest;
import com.k.medtour.domain.chat.dto.ReadResponse;
import com.k.medtour.domain.chat.dto.SosRequest;
import com.k.medtour.domain.chat.dto.SosResponse;
import com.k.medtour.domain.chat.enums.ChatMessageType;
import com.k.medtour.domain.chat.enums.ChatRoomType;
import com.k.medtour.domain.chat.service.ChatService;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("POST /api/v1/chat/rooms")
    class CreateRoomApiTest {

        @Test
        @DisplayName("성공 - 채팅방을 생성하면 201을 반환한다")
        @WithMockUser(roles = "ADMIN")
        void createRoom_success() throws Exception {
            // Given
            ChatRoomCreateRequest request = new ChatRoomCreateRequest(
                    ChatRoomType.PATIENT_AGENCY,
                    List.of(
                            new ChatRoomCreateRequest.ParticipantInfo(1L, "ADMIN"),
                            new ChatRoomCreateRequest.ParticipantInfo(5L, "PATIENT")
                    ),
                    100L, "en"
            );

            ChatRoomResponse response = new ChatRoomResponse(
                    "room-100-patient_agency-1-5",
                    ChatRoomType.PATIENT_AGENCY,
                    List.of(
                            new ChatRoomResponse.ParticipantResponse(1L, "Admin", "ADMIN"),
                            new ChatRoomResponse.ParticipantResponse(5L, "John", "PATIENT")
                    ),
                    LocalDateTime.now()
            );

            given(chatService.createRoom(any())).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/chat/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.roomId").value("room-100-patient_agency-1-5"))
                    .andExpect(jsonPath("$.data.type").value("PATIENT_AGENCY"))
                    .andExpect(jsonPath("$.data.participants").isArray())
                    .andExpect(jsonPath("$.data.participants.length()").value(2));
        }

        @Test
        @DisplayName("실패 - 중복 채팅방이면 409를 반환한다")
        @WithMockUser(roles = "ADMIN")
        void createRoom_fail_conflict() throws Exception {
            // Given
            ChatRoomCreateRequest request = new ChatRoomCreateRequest(
                    ChatRoomType.PATIENT_AGENCY,
                    List.of(
                            new ChatRoomCreateRequest.ParticipantInfo(1L, "ADMIN"),
                            new ChatRoomCreateRequest.ParticipantInfo(5L, "PATIENT")
                    ),
                    100L, "en"
            );

            given(chatService.createRoom(any()))
                    .willThrow(new BusinessException(ErrorCode.CHAT_ROOM_ALREADY_EXISTS));

            // When & Then
            mockMvc.perform(post("/api/v1/chat/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/chat/rooms/{roomId}/messages")
    class GetMessagesApiTest {

        @Test
        @DisplayName("성공 - 메시지 이력을 조회한다")
        @WithMockUser(roles = "PATIENT")
        void getMessages_success() throws Exception {
            // Given
            ChatMessageListResponse response = new ChatMessageListResponse(
                    List.of(new ChatMessageResponse(
                            "msg-001", "room-100-patient_agency-1-5",
                            5L, "John", "PATIENT",
                            ChatMessageType.TEXT, "Hello", Map.of(),
                            LocalDateTime.now(), List.of(1L)
                    )),
                    "msg-000", true
            );

            given(chatService.getMessages(anyString(), any(), anyInt(), any(), any()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/chat/rooms/room-100-patient_agency-1-5/messages")
                            .param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.messages").isArray())
                    .andExpect(jsonPath("$.data.messages[0].content").value("Hello"))
                    .andExpect(jsonPath("$.data.hasMore").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/chat/rooms/{roomId}/messages")
    class SendMessageApiTest {

        @Test
        @DisplayName("성공 - 텍스트 메시지를 전송하면 201을 반환한다")
        @WithMockUser(roles = "PATIENT")
        void sendMessage_success() throws Exception {
            // Given
            ChatMessageSendRequest request = new ChatMessageSendRequest(
                    ChatMessageType.TEXT, "Hello", "en");

            ChatMessageResponse response = new ChatMessageResponse(
                    "msg-003", "room-100-patient_agency-1-5",
                    5L, "John", "PATIENT",
                    ChatMessageType.TEXT, "Hello", Map.of("ko", "안녕"),
                    LocalDateTime.now(), List.of()
            );

            given(chatService.sendMessage(anyString(), any(), any(), any()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/chat/rooms/room-100-patient_agency-1-5/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").value("Hello"))
                    .andExpect(jsonPath("$.data.type").value("TEXT"));
        }

        @Test
        @DisplayName("실패 - 참여자가 아닌 경우 403을 반환한다")
        @WithMockUser(roles = "PATIENT")
        void sendMessage_fail_notParticipant() throws Exception {
            // Given
            ChatMessageSendRequest request = new ChatMessageSendRequest(
                    ChatMessageType.TEXT, "Hello", "en");

            given(chatService.sendMessage(anyString(), any(), any(), any()))
                    .willThrow(new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT));

            // When & Then
            mockMvc.perform(post("/api/v1/chat/rooms/room-100-patient_agency-1-5/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/chat/rooms/{roomId}/read")
    class MarkAsReadApiTest {

        @Test
        @DisplayName("성공 - 읽음 처리를 수행한다")
        @WithMockUser(roles = "PATIENT")
        void markAsRead_success() throws Exception {
            // Given
            ReadRequest request = new ReadRequest("msg-003");
            ReadResponse response = new ReadResponse("room-100-patient_agency-1-5", 0);

            given(chatService.markAsRead(anyString(), any(), any(), any()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/chat/rooms/room-100-patient_agency-1-5/read")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.unreadCount").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/chat/admin/monitor")
    class MonitorApiTest {

        @Test
        @DisplayName("성공 - 관리자 멀티챗 관제를 조회한다")
        @WithMockUser(roles = "ADMIN")
        void monitorRooms_success() throws Exception {
            // Given
            MonitorResponse response = new MonitorResponse(
                    25, 15, 42,
                    List.of(new MonitorResponse.MonitorRoomInfo(
                            "room-100-patient_agency-1-5",
                            ChatRoomType.PATIENT_AGENCY,
                            new MonitorResponse.PatientInfo(5L, "John Doe"),
                            100L,
                            new MonitorResponse.MonitorLastMessage(
                                    "Hello", "안녕", LocalDateTime.now()),
                            3, false
                    )),
                    0, 20, 25, 2
            );

            given(chatService.monitorRooms(any(), anyInt(), anyInt()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/chat/admin/monitor"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalRooms").value(25))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].roomId").value("room-100-patient_agency-1-5"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/chat/sos")
    class SosApiTest {

        @Test
        @DisplayName("성공 - SOS를 전송하면 201을 반환한다")
        @WithMockUser(roles = "STAFF")
        void sendSos_success() throws Exception {
            // Given
            SosRequest request = new SosRequest(
                    "환자 의식 불명", "room-100-staff_agency-1-10", 100L);

            SosResponse response = new SosResponse(
                    "msg-sos-001", "room-100-staff_agency-1-10",
                    "[SOS] 환자 의식 불명", LocalDateTime.now());

            given(chatService.sendSos(any(), any(), any()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/chat/sos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").value("[SOS] 환자 의식 불명"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/chat/rooms/{roomId}/messages/file")
    class SendFileMessageApiTest {

        @Test
        @DisplayName("성공 - 파일 메시지를 전송하면 201을 반환한다")
        @WithMockUser(roles = "PATIENT")
        void sendFileMessage_success() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "xray.jpg", "image/jpeg", new byte[1024]);

            ChatFileMessageResponse response = new ChatFileMessageResponse(
                    "msg-004", "room-100-patient_agency-1-5",
                    ChatMessageType.FILE,
                    new ChatFileMessageResponse.FileInfo(
                            400L, "xray.jpg", 1024L, "image/jpeg",
                            "https://cdn.example.com/xray.jpg", true,
                            LocalDateTime.now().plusDays(30)),
                    "X-ray photo",
                    LocalDateTime.now()
            );

            given(chatService.sendFileMessage(anyString(), any(), any(), any(), any(), any()))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(multipart("/api/v1/chat/rooms/room-100-patient_agency-1-5/messages/file")
                            .file(file)
                            .param("caption", "X-ray photo")
                            .param("isSecure", "true"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.type").value("FILE"))
                    .andExpect(jsonPath("$.data.file.fileName").value("xray.jpg"))
                    .andExpect(jsonPath("$.data.file.isSecure").value(true));
        }
    }
}
