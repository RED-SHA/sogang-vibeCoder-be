package com.k.medtour.domain.chat.service;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.admin.entity.Role;
import com.k.medtour.domain.admin.repository.MemberRepository;
import com.k.medtour.domain.chat.dto.ChatMessageListResponse;
import com.k.medtour.domain.chat.dto.ChatMessageResponse;
import com.k.medtour.domain.chat.dto.ChatMessageSendRequest;
import com.k.medtour.domain.chat.dto.ChatRoomCreateRequest;
import com.k.medtour.domain.chat.dto.ChatRoomResponse;
import com.k.medtour.domain.chat.dto.ReadRequest;
import com.k.medtour.domain.chat.dto.ReadResponse;
import com.k.medtour.domain.chat.dto.SosRequest;
import com.k.medtour.domain.chat.dto.SosResponse;
import com.k.medtour.domain.chat.entity.ChatMessage;
import com.k.medtour.domain.chat.entity.ChatRoom;
import com.k.medtour.domain.chat.entity.ChatRoomParticipant;
import com.k.medtour.domain.chat.enums.ChatMessageType;
import com.k.medtour.domain.chat.enums.ChatRoomType;
import com.k.medtour.domain.chat.repository.ChatMessageRepository;
import com.k.medtour.domain.chat.repository.ChatRoomParticipantRepository;
import com.k.medtour.domain.chat.repository.ChatRoomRepository;
import com.k.medtour.domain.file.service.FileService;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import com.k.medtour.infra.translation.TranslationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomParticipantRepository participantRepository;

    @Mock
    private ChatMessageRepository messageRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FileService fileService;

    @Mock
    private TranslationService translationService;

    private Member createMember(Long id, String name, String roleName) {
        Role role = Role.builder().name(roleName).description(roleName).build();
        return Member.builder()
                .email(name.toLowerCase() + "@test.com")
                .name(name)
                .role(role)
                .language("en")
                .build();
    }

    @Nested
    @DisplayName("채팅방 생성")
    class CreateRoomTest {

        @Test
        @DisplayName("성공 - 새 채팅방을 생성한다")
        void createRoom_success() {
            // Given
            ChatRoomCreateRequest request = new ChatRoomCreateRequest(
                    ChatRoomType.PATIENT_AGENCY,
                    List.of(
                            new ChatRoomCreateRequest.ParticipantInfo(1L, "ADMIN"),
                            new ChatRoomCreateRequest.ParticipantInfo(5L, "PATIENT")
                    ),
                    100L,
                    "en"
            );

            given(chatRoomRepository.existsByRoomId(anyString())).willReturn(false);
            given(chatRoomRepository.save(any(ChatRoom.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(createMember(1L, "Admin", "ADMIN")));
            given(memberRepository.findById(5L))
                    .willReturn(Optional.of(createMember(5L, "John", "PATIENT")));
            given(participantRepository.save(any(ChatRoomParticipant.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // When
            ChatRoomResponse response = chatService.createRoom(request);

            // Then
            assertThat(response.roomId()).startsWith("room-100-patient_agency-");
            assertThat(response.type()).isEqualTo(ChatRoomType.PATIENT_AGENCY);
            assertThat(response.participants()).hasSize(2);
            verify(chatRoomRepository).save(any(ChatRoom.class));
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 채팅방이면 예외 발생")
        void createRoom_fail_alreadyExists() {
            // Given
            ChatRoomCreateRequest request = new ChatRoomCreateRequest(
                    ChatRoomType.PATIENT_AGENCY,
                    List.of(
                            new ChatRoomCreateRequest.ParticipantInfo(1L, "ADMIN"),
                            new ChatRoomCreateRequest.ParticipantInfo(5L, "PATIENT")
                    ),
                    100L,
                    "en"
            );

            given(chatRoomRepository.existsByRoomId(anyString())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> chatService.createRoom(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("메시지 전송")
    class SendMessageTest {

        @Test
        @DisplayName("성공 - 텍스트 메시지를 전송한다")
        void sendMessage_success() {
            // Given
            String roomId = "room-100-patient_agency-1-5";
            ChatMessageSendRequest request = new ChatMessageSendRequest(
                    ChatMessageType.TEXT, "Hello", "en");

            ChatRoom chatRoom = ChatRoom.builder()
                    .roomId(roomId)
                    .type(ChatRoomType.PATIENT_AGENCY)
                    .language("ko")
                    .build();

            given(participantRepository.existsByChatRoom_RoomIdAndMemberId(roomId, 5L))
                    .willReturn(true);
            given(chatRoomRepository.findById(roomId))
                    .willReturn(Optional.of(chatRoom));
            given(memberRepository.findById(5L))
                    .willReturn(Optional.of(createMember(5L, "John", "PATIENT")));
            given(translationService.translate("Hello", "en", "ko"))
                    .willReturn(Map.of());
            given(messageRepository.save(any(ChatMessage.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            given(participantRepository.findMemberIdsByRoomId(roomId))
                    .willReturn(List.of(1L, 5L));
            given(participantRepository.findAllByChatRoom_RoomId(roomId))
                    .willReturn(List.of());

            // When
            ChatMessageResponse response = chatService.sendMessage(
                    roomId, request, 5L, "PATIENT");

            // Then
            assertThat(response.content()).isEqualTo("Hello");
            assertThat(response.senderName()).isEqualTo("John");
            assertThat(response.type()).isEqualTo(ChatMessageType.TEXT);
            verify(messageRepository).save(any(ChatMessage.class));
        }

        @Test
        @DisplayName("실패 - 참여자가 아닌 경우 예외 발생")
        void sendMessage_fail_notParticipant() {
            // Given
            String roomId = "room-100-patient_agency-1-5";
            ChatMessageSendRequest request = new ChatMessageSendRequest(
                    ChatMessageType.TEXT, "Hello", "en");

            given(participantRepository.existsByChatRoom_RoomIdAndMemberId(roomId, 99L))
                    .willReturn(false);

            // When & Then
            assertThatThrownBy(() -> chatService.sendMessage(roomId, request, 99L, "PATIENT"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CHAT_NOT_PARTICIPANT);
        }

        @Test
        @DisplayName("성공 - ADMIN은 참여자가 아니어도 메시지 전송 가능")
        void sendMessage_success_admin() {
            // Given
            String roomId = "room-100-patient_agency-1-5";
            ChatMessageSendRequest request = new ChatMessageSendRequest(
                    ChatMessageType.TEXT, "Admin message", "ko");

            ChatRoom chatRoom = ChatRoom.builder()
                    .roomId(roomId)
                    .type(ChatRoomType.PATIENT_AGENCY)
                    .language("en")
                    .build();

            given(chatRoomRepository.findById(roomId))
                    .willReturn(Optional.of(chatRoom));
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(createMember(1L, "Admin", "ADMIN")));
            given(translationService.translate("Admin message", "ko", "en"))
                    .willReturn(Map.of());
            given(messageRepository.save(any(ChatMessage.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            given(participantRepository.findMemberIdsByRoomId(roomId))
                    .willReturn(List.of(1L, 5L));
            given(participantRepository.findAllByChatRoom_RoomId(roomId))
                    .willReturn(List.of());

            // When
            ChatMessageResponse response = chatService.sendMessage(
                    roomId, request, 1L, "ADMIN");

            // Then
            assertThat(response.content()).isEqualTo("Admin message");
        }
    }

    @Nested
    @DisplayName("읽음 처리")
    class MarkAsReadTest {

        @Test
        @DisplayName("성공 - 읽음 처리를 수행한다")
        void markAsRead_success() {
            // Given
            String roomId = "room-100-patient_agency-1-5";
            ReadRequest request = new ReadRequest("msg-003");

            ChatRoom chatRoom = ChatRoom.builder()
                    .roomId(roomId)
                    .type(ChatRoomType.PATIENT_AGENCY)
                    .build();

            ChatRoomParticipant participant = ChatRoomParticipant.builder()
                    .chatRoom(chatRoom)
                    .memberId(5L)
                    .build();

            given(participantRepository.existsByChatRoom_RoomIdAndMemberId(roomId, 5L))
                    .willReturn(true);
            given(participantRepository.findByChatRoom_RoomIdAndMemberId(roomId, 5L))
                    .willReturn(Optional.of(participant));
            given(messageRepository.countUnreadMessages(roomId, "msg-003"))
                    .willReturn(0L);

            // When
            ReadResponse response = chatService.markAsRead(roomId, request, 5L, "PATIENT");

            // Then
            assertThat(response.roomId()).isEqualTo(roomId);
            assertThat(response.unreadCount()).isEqualTo(0);
            assertThat(participant.getLastReadMessageId()).isEqualTo("msg-003");
        }
    }

    @Nested
    @DisplayName("메시지 이력 조회")
    class GetMessagesTest {

        @Test
        @DisplayName("성공 - 커서 없이 최신 메시지를 조회한다")
        void getMessages_success_noCursor() {
            // Given
            String roomId = "room-100-patient_agency-1-5";

            ChatRoom chatRoom = ChatRoom.builder()
                    .roomId(roomId)
                    .type(ChatRoomType.PATIENT_AGENCY)
                    .build();

            ChatMessage msg = ChatMessage.builder()
                    .id("msg-001")
                    .chatRoom(chatRoom)
                    .senderId(5L)
                    .senderName("John")
                    .senderRole("PATIENT")
                    .type(ChatMessageType.TEXT)
                    .content("Hello")
                    .sentAt(LocalDateTime.now())
                    .build();

            given(participantRepository.existsByChatRoom_RoomIdAndMemberId(roomId, 5L))
                    .willReturn(true);
            given(messageRepository.findByRoomIdOrderBySentAtDesc(eq(roomId), any()))
                    .willReturn(List.of(msg));
            given(participantRepository.findMemberIdsByRoomId(roomId))
                    .willReturn(List.of(1L, 5L));
            given(participantRepository.findAllByChatRoom_RoomId(roomId))
                    .willReturn(List.of());

            // When
            ChatMessageListResponse response = chatService.getMessages(
                    roomId, null, 50, 5L, "PATIENT");

            // Then
            assertThat(response.messages()).hasSize(1);
            assertThat(response.messages().get(0).content()).isEqualTo("Hello");
            assertThat(response.hasMore()).isFalse();
        }
    }

    @Nested
    @DisplayName("SOS 긴급 호출")
    class SosTest {

        @Test
        @DisplayName("성공 - SOS 메시지를 전송한다")
        void sendSos_success() {
            // Given
            String roomId = "room-100-staff_agency-1-10";
            SosRequest request = new SosRequest("환자 의식 불명", roomId, 100L);

            ChatRoom chatRoom = ChatRoom.builder()
                    .roomId(roomId)
                    .type(ChatRoomType.STAFF_AGENCY)
                    .build();

            given(chatRoomRepository.findById(roomId))
                    .willReturn(Optional.of(chatRoom));
            given(memberRepository.findById(10L))
                    .willReturn(Optional.of(createMember(10L, "Driver Kim", "STAFF")));
            given(messageRepository.save(any(ChatMessage.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // When
            SosResponse response = chatService.sendSos(request, 10L, "STAFF");

            // Then
            assertThat(response.roomId()).isEqualTo(roomId);
            assertThat(response.content()).contains("[SOS]");
            assertThat(response.content()).contains("환자 의식 불명");
            verify(messageRepository).save(any(ChatMessage.class));
        }

        @Test
        @DisplayName("실패 - roomId 없이 SOS 전송 시 예외 발생")
        void sendSos_fail_noRoomId() {
            // Given
            SosRequest request = new SosRequest("긴급 상황", null, 100L);

            // When & Then
            assertThatThrownBy(() -> chatService.sendSos(request, 10L, "STAFF"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
    }
}
