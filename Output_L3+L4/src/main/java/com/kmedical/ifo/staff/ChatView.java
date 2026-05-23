package com.kmedical.ifo.staff;

import com.kmedical.control.ChatController;
import com.kmedical.dto.chat.AttachmentDTO;
import com.kmedical.dto.chat.ChatMessageDTO;
import com.kmedical.dto.chat.ChatMessageSendRequestDTO;

import java.util.List;

/**
 * IFO-S08 — ChatView (Staff)
 * UC: UC-S09 (스태프 채팅)
 * 책임: 스태프가 환자·코디네이터와 채팅하고 파일을 첨부하는 UI 진입점.
 */
public class ChatView {

    private final ChatController chatController;

    public ChatView(ChatController chatController) {
        this.chatController = chatController;
    }

    /**
     * 스태프가 메시지를 발송한다.
     * Actor Action: Staff sends a chat message.
     */
    public ChatMessageDTO sendMessage(ChatMessageSendRequestDTO request) {
        return chatController.sendMessage(request);
    }

    /**
     * 스태프가 대화 내역을 조회한다.
     * Actor Action: Staff views the conversation history.
     */
    public List<ChatMessageDTO> viewMessages(String conversationId) {
        return chatController.getMessages(conversationId);
    }

    /**
     * 스태프가 파일을 첨부한다.
     * Actor Action: Staff attaches a file to a message.
     */
    public AttachmentDTO attachFile(String chatMessageId, AttachmentDTO dto) {
        return chatController.addAttachment(chatMessageId, dto);
    }
}
