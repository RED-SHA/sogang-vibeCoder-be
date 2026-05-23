package com.kmedical.ifo.admin;

import com.kmedical.control.ChatController;
import com.kmedical.domain.enums.ConversationType;
import com.kmedical.dto.chat.ChatMessageDTO;
import com.kmedical.dto.chat.ChatMessageSendRequestDTO;
import com.kmedical.dto.chat.ConversationDTO;

import java.util.List;

/**
 * IFO-A10 — ChatConsoleView
 * UC: UC-A09 (관리자 채팅 콘솔)
 * 책임: 관리자가 대화방을 생성하고 메시지를 주고받는 UI 진입점.
 */
public class ChatConsoleView {

    private final ChatController chatController;

    public ChatConsoleView(ChatController chatController) {
        this.chatController = chatController;
    }

    /**
     * 관리자가 대화방을 개설한다.
     * Actor Action: Admin creates a new conversation with patient or staff.
     */
    public ConversationDTO openConversation(String patientJourneyId, ConversationType type,
                                             List<String> participantIds, String coordinatorId) {
        return chatController.createConversation(patientJourneyId, type, participantIds, coordinatorId);
    }

    /**
     * 관리자가 메시지를 발송한다.
     * Actor Action: Admin sends a message in the conversation.
     */
    public ChatMessageDTO sendMessage(ChatMessageSendRequestDTO request) {
        return chatController.sendMessage(request);
    }

    /**
     * 관리자가 대화 내역을 조회한다.
     * Actor Action: Admin views the message history.
     */
    public List<ChatMessageDTO> viewMessages(String conversationId) {
        return chatController.getMessages(conversationId);
    }

    /**
     * 관리자가 대화방을 종료한다.
     * Actor Action: Admin closes the conversation.
     */
    public void closeConversation(String conversationId) {
        chatController.closeConversation(conversationId);
    }
}
