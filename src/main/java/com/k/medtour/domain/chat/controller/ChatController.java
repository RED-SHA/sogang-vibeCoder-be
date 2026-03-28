package com.k.medtour.domain.chat.controller;

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
import com.k.medtour.domain.chat.enums.ChatRoomType;
import com.k.medtour.domain.chat.service.ChatService;
import com.k.medtour.global.auth.UserPrincipal;
import com.k.medtour.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 채팅방 생성
     */
    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<ChatRoomResponse> createRoom(
            @Valid @RequestBody ChatRoomCreateRequest request) {
        return ApiResponse.success("채팅방 생성 완료", chatService.createRoom(request));
    }

    /**
     * 내 채팅방 목록 조회
     */
    @GetMapping("/rooms")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<List<ChatRoomListResponse>> getMyRooms(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) ChatRoomType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("조회 성공",
                chatService.getMyRooms(principal.memberId(), type, page, size));
    }

    /**
     * 메시지 이력 조회 (커서 기반)
     */
    @GetMapping("/rooms/{roomId}/messages")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<ChatMessageListResponse> getMessages(
            @PathVariable String roomId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("조회 성공",
                chatService.getMessages(roomId, cursor, size, principal.memberId(), principal.role()));
    }

    /**
     * 텍스트 메시지 전송
     */
    @PostMapping("/rooms/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<ChatMessageResponse> sendMessage(
            @PathVariable String roomId,
            @Valid @RequestBody ChatMessageSendRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("전송 완료",
                chatService.sendMessage(roomId, request, principal.memberId(), principal.role()));
    }

    /**
     * 파일 메시지 전송
     */
    @PostMapping(value = "/rooms/{roomId}/messages/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<ChatFileMessageResponse> sendFileMessage(
            @PathVariable String roomId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) Boolean isSecure,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("파일 전송 완료",
                chatService.sendFileMessage(roomId, file, caption, isSecure,
                        principal.memberId(), principal.role()));
    }

    /**
     * 읽음 처리
     */
    @PostMapping("/rooms/{roomId}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<ReadResponse> markAsRead(
            @PathVariable String roomId,
            @Valid @RequestBody ReadRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("읽음 처리 완료",
                chatService.markAsRead(roomId, request, principal.memberId(), principal.role()));
    }

    /**
     * 관리자 멀티챗 관제 조회
     */
    @GetMapping("/admin/monitor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApiResponse<MonitorResponse> monitorRooms(
            @RequestParam(required = false) ChatRoomType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("조회 성공", chatService.monitorRooms(type, page, size));
    }

    /**
     * 긴급 호출 (SOS)
     */
    @PostMapping("/sos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('PATIENT') or hasRole('STAFF')")
    public ApiResponse<SosResponse> sendSos(
            @Valid @RequestBody SosRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("SOS 전송 완료",
                chatService.sendSos(request, principal.memberId(), principal.role()));
    }
}
