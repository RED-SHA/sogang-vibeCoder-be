package com.kmedical.control;

import com.kmedical.adapter.TranslationAdapter;
import com.kmedical.domain.entity.Attachment;
import com.kmedical.domain.entity.ChatMessage;
import com.kmedical.domain.entity.Conversation;
import com.kmedical.domain.enums.ConversationType;
import com.kmedical.dto.chat.AttachmentDTO;
import com.kmedical.dto.chat.ChatMessageDTO;
import com.kmedical.dto.chat.ChatMessageSendRequestDTO;
import com.kmedical.dto.chat.ConversationDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C12 — ChatController
 * 책임: 채팅 생성/송수신, 자동 번역 처리.
 * UC: UC-A09, UC-P09, UC-S09, UC-T01
 */
public class ChatController {

    private final TranslationAdapter translationAdapter;
    private final Map<String, Conversation> conversationStore = new HashMap<>();
    private final Map<String, List<ChatMessage>> messageStore = new HashMap<>();
    private final Map<String, List<Attachment>> attachmentStore = new HashMap<>();

    public ChatController(TranslationAdapter translationAdapter) {
        this.translationAdapter = translationAdapter;
    }

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 대화방을 생성한다.
     * System Response: 입력 검증 → Conversation 저장 → DTO 반환
     */
    public ConversationDTO createConversation(String patientJourneyId, ConversationType type,
                                               List<String> participantIds, String coordinatorId) {
        guardNotClosedDown();
        if (patientJourneyId == null || type == null) {
            throw new IllegalArgumentException("Conversation creation data is incomplete.");
        }

        Conversation conv = new Conversation();
        conv.setConversationId(UUID.randomUUID().toString());
        conv.setPatientJourneyId(patientJourneyId);
        conv.setConversationType(type);
        conv.setParticipantIds(participantIds != null ? participantIds : new ArrayList<>());
        conv.setAssignedCoordinatorId(coordinatorId);
        conv.setIsActive(true);
        conv.setCreatedAt(LocalDateTime.now());

        conversationStore.put(conv.getConversationId(), conv);
        messageStore.put(conv.getConversationId(), new ArrayList<>());
        return toConvDTO(conv);
    }

    /**
     * 메시지를 발송하고 자동 번역을 수행한다 (UC-T01).
     * System Response: 메시지 저장 → TranslationAdapter 번역 요청 → 번역문 저장
     */
    public ChatMessageDTO sendMessage(ChatMessageSendRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getConversationId() == null || request.getSenderId() == null) {
            throw new IllegalArgumentException("Message send request is incomplete.");
        }

        Conversation conv = findConversation(request.getConversationId());
        if (!Boolean.TRUE.equals(conv.getIsActive())) {
            throw new IllegalStateException("Conversation is not active.");
        }

        ChatMessage msg = new ChatMessage();
        msg.setChatMessageId(UUID.randomUUID().toString());
        msg.setConversationId(request.getConversationId());
        msg.setSenderId(request.getSenderId());
        msg.setOriginalText(request.getOriginalText());
        msg.setOriginalLang(request.getOriginalLang());
        msg.setSentAt(LocalDateTime.now());

        try {
            String translated = translationAdapter.translate(
                    request.getOriginalText(), request.getOriginalLang(), null);
            msg.setTranslatedText(translated);
        } catch (Exception ignored) {
        }

        messageStore.get(request.getConversationId()).add(msg);
        return toMsgDTO(msg);
    }

    /**
     * 대화방의 메시지 목록을 조회한다.
     */
    public List<ChatMessageDTO> getMessages(String conversationId) {
        guardNotClosedDown();
        findConversation(conversationId);
        List<ChatMessageDTO> result = new ArrayList<>();
        for (ChatMessage m : messageStore.getOrDefault(conversationId, new ArrayList<>())) {
            result.add(toMsgDTO(m));
        }
        return result;
    }

    /**
     * 파일 첨부를 등록한다.
     * System Response: 메시지 존재 확인 → Attachment 저장
     */
    public AttachmentDTO addAttachment(String chatMessageId, AttachmentDTO dto) {
        guardNotClosedDown();
        if (chatMessageId == null || dto == null || dto.getFileUrl() == null) {
            throw new IllegalArgumentException("Attachment data is incomplete.");
        }

        Attachment att = new Attachment();
        att.setAttachmentId(UUID.randomUUID().toString());
        att.setChatMessageId(chatMessageId);
        att.setFileUrl(dto.getFileUrl());
        att.setFileType(dto.getFileType());
        att.setFileSizeBytes(dto.getFileSizeBytes());
        att.setUploadedAt(LocalDateTime.now());

        attachmentStore.computeIfAbsent(chatMessageId, k -> new ArrayList<>()).add(att);

        AttachmentDTO result = new AttachmentDTO();
        result.setAttachmentId(att.getAttachmentId());
        result.setChatMessageId(att.getChatMessageId());
        result.setFileUrl(att.getFileUrl());
        result.setFileType(att.getFileType());
        result.setFileSizeBytes(att.getFileSizeBytes());
        result.setUploadedAt(att.getUploadedAt());
        return result;
    }

    /**
     * 대화방을 종료한다.
     */
    public void closeConversation(String conversationId) {
        guardNotClosedDown();
        Conversation conv = findConversation(conversationId);
        conv.setIsActive(false);
        conv.setClosedAt(LocalDateTime.now());
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private Conversation findConversation(String id) {
        Conversation c = conversationStore.get(id);
        if (c == null) throw new IllegalArgumentException("Conversation not found: " + id);
        return c;
    }

    private ConversationDTO toConvDTO(Conversation c) {
        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(c.getConversationId());
        dto.setPatientJourneyId(c.getPatientJourneyId());
        dto.setConversationType(c.getConversationType());
        dto.setParticipantIds(c.getParticipantIds());
        dto.setAssignedCoordinatorId(c.getAssignedCoordinatorId());
        dto.setIsActive(c.getIsActive());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setClosedAt(c.getClosedAt());
        return dto;
    }

    private ChatMessageDTO toMsgDTO(ChatMessage m) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setChatMessageId(m.getChatMessageId());
        dto.setConversationId(m.getConversationId());
        dto.setSenderId(m.getSenderId());
        dto.setOriginalText(m.getOriginalText());
        dto.setOriginalLang(m.getOriginalLang());
        dto.setTranslatedText(m.getTranslatedText());
        dto.setTranslatedLang(m.getTranslatedLang());
        dto.setSentAt(m.getSentAt());
        dto.setReadAt(m.getReadAt());
        return dto;
    }
}
