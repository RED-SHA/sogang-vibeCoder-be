package com.k.medtour.domain.chat.entity;

import com.k.medtour.domain.chat.enums.ChatMessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    @Column(name = "id", length = 100)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "sender_name", nullable = false, length = 100)
    private String senderName;

    @Column(name = "sender_role", nullable = false, length = 30)
    private String senderRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChatMessageType type;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "translated_content", columnDefinition = "jsonb")
    private Map<String, String> translatedContent = new HashMap<>();

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "caption", length = 500)
    private String caption;

    @Column(name = "is_secure")
    private Boolean isSecure;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Builder
    public ChatMessage(String id, ChatRoom chatRoom, Long senderId, String senderName,
                       String senderRole, ChatMessageType type, String content,
                       Map<String, String> translatedContent, Long fileId,
                       String caption, Boolean isSecure, LocalDateTime sentAt) {
        this.id = id;
        this.chatRoom = chatRoom;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.type = type;
        this.content = content;
        this.translatedContent = translatedContent != null ? translatedContent : new HashMap<>();
        this.fileId = fileId;
        this.caption = caption;
        this.isSecure = isSecure;
        this.sentAt = sentAt != null ? sentAt : LocalDateTime.now();
    }
}
