package com.kmedical.ifo.patient;

import com.kmedical.control.ChatController;
import com.kmedical.dto.chat.AttachmentDTO;
import com.kmedical.dto.chat.ChatMessageDTO;
import com.kmedical.dto.chat.ChatMessageSendRequestDTO;

import java.util.List;

/**
 * IFO-P09 — ChatView (Patient)
 * UC: UC-P09 (환자 채팅)
 * 책임: 환자가 코디네이터/스태프와 채팅하고 파일을 첨부하는 UI 진입점.
 */
public class ChatView {

    private final ChatController chatController;

    public ChatView(ChatController chatController) {
        this.chatController = chatController;
    }

    /**
     * 환자가 메시지를 발송한다.
     * Actor Action: Patient sends a chat message.
     */
    public ChatMessageDTO sendMessage(ChatMessageSendRequestDTO request) {
        return chatController.sendMessage(request);
    }

    /**
     * 환자가 대화 내역을 조회한다.
     * Actor Action: Patient views the conversation history.
     */
    public List<ChatMessageDTO> viewMessages(String conversationId) {
        return chatController.getMessages(conversationId);
    }

    /**
     * 환자가 파일을 첨부한다.
     * Actor Action: Patient attaches a file to a message.
     */
    public AttachmentDTO attachFile(String chatMessageId, AttachmentDTO dto) {
        return chatController.addAttachment(chatMessageId, dto);
    }
}
