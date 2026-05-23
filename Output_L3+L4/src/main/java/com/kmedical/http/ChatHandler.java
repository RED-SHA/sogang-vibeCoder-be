package com.kmedical.http;

import com.kmedical.control.ChatController;
import com.kmedical.domain.enums.ConversationType;
import com.kmedical.domain.enums.Language;
import com.kmedical.dto.chat.ChatMessageDTO;
import com.kmedical.dto.chat.ChatMessageSendRequestDTO;
import com.kmedical.dto.chat.ConversationDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * SRV-C12 ChatController HTTP 매핑
 *
 * POST /api/chat/conversations               대화방 생성
 * POST /api/chat/messages                    메시지 발송
 * GET  /api/chat/messages?conversationId=    메시지 목록 조회
 * POST /api/chat/close                       대화방 종료
 */
public class ChatHandler extends BaseHandler {

    private final ChatController chatController;

    public ChatHandler(ChatController chatController) {
        this.chatController = chatController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();

        if ("POST".equals(method) && "/api/chat/conversations".equals(path)) {
            handleCreateConversation(ex);
        } else if ("POST".equals(method) && "/api/chat/messages".equals(path)) {
            handleSendMessage(ex);
        } else if ("GET".equals(method) && "/api/chat/messages".equals(path)) {
            handleGetMessages(ex);
        } else if ("POST".equals(method) && "/api/chat/close".equals(path)) {
            handleClose(ex);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleCreateConversation(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        ConversationType type = body.get("conversationType") != null
                ? ConversationType.valueOf(body.get("conversationType")) : null;
        List<String> participants = body.get("participantIds") != null
                ? Arrays.asList(body.get("participantIds").split(",")) : new ArrayList<>();
        ConversationDTO result = chatController.createConversation(
                body.get("patientJourneyId"), type, participants, body.get("coordinatorId"));
        sendJson(ex, 201, conversationToMap(result));
    }

    private void handleSendMessage(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        ChatMessageSendRequestDTO req = new ChatMessageSendRequestDTO();
        req.setConversationId(body.get("conversationId"));
        req.setSenderId(body.get("senderId"));
        req.setOriginalText(body.get("originalText"));
        if (body.get("originalLang") != null) req.setOriginalLang(Language.valueOf(body.get("originalLang")));
        sendJson(ex, 201, messageToMap(chatController.sendMessage(req)));
    }

    private void handleGetMessages(HttpExchange ex) throws IOException {
        String conversationId = queryParam(ex, "conversationId");
        if (conversationId == null) { sendError(ex, 400, "Query parameter 'conversationId' is required."); return; }
        List<ChatMessageDTO> list = chatController.getMessages(conversationId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ChatMessageDTO m : list) items.add(messageToMap(m));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private void handleClose(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        String conversationId = body.get("conversationId");
        if (conversationId == null) { sendError(ex, 400, "conversationId is required."); return; }
        chatController.closeConversation(conversationId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("result", "closed");
        sendJson(ex, 200, m);
    }

    private Map<String, Object> conversationToMap(ConversationDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("conversationId",       dto.getConversationId());
        m.put("patientJourneyId",     dto.getPatientJourneyId());
        m.put("conversationType",     dto.getConversationType() != null ? dto.getConversationType().name() : null);
        m.put("participantIds",       dto.getParticipantIds());
        m.put("assignedCoordinatorId", dto.getAssignedCoordinatorId());
        m.put("isActive",             dto.getIsActive());
        m.put("createdAt",            dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null);
        m.put("closedAt",             dto.getClosedAt() != null ? dto.getClosedAt().toString() : null);
        return m;
    }

    private Map<String, Object> messageToMap(ChatMessageDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("chatMessageId",  dto.getChatMessageId());
        m.put("conversationId", dto.getConversationId());
        m.put("senderId",       dto.getSenderId());
        m.put("originalText",   dto.getOriginalText());
        m.put("originalLang",   dto.getOriginalLang());
        m.put("translatedText", dto.getTranslatedText());
        m.put("translatedLang", dto.getTranslatedLang());
        m.put("sentAt",         dto.getSentAt() != null ? dto.getSentAt().toString() : null);
        m.put("readAt",         dto.getReadAt() != null ? dto.getReadAt().toString() : null);
        return m;
    }
}
