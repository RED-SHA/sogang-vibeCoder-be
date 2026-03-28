package com.k.medtour.domain.chat.repository;

import com.k.medtour.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatRoom.roomId = :roomId " +
            "ORDER BY m.sentAt DESC")
    List<ChatMessage> findByRoomIdOrderBySentAtDesc(
            @Param("roomId") String roomId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.chatRoom.roomId = :roomId AND m.sentAt < " +
            "(SELECT cm.sentAt FROM ChatMessage cm WHERE cm.id = :cursor) " +
            "ORDER BY m.sentAt DESC")
    List<ChatMessage> findByRoomIdAndCursorOrderBySentAtDesc(
            @Param("roomId") String roomId,
            @Param("cursor") String cursor,
            Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.chatRoom.roomId = :roomId AND m.sentAt > " +
            "(SELECT COALESCE(" +
            "(SELECT cm.sentAt FROM ChatMessage cm WHERE cm.id = :lastReadMessageId), " +
            "CAST('1970-01-01' AS timestamp)))")
    long countUnreadMessages(
            @Param("roomId") String roomId,
            @Param("lastReadMessageId") String lastReadMessageId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.roomId = :roomId")
    long countByRoomId(@Param("roomId") String roomId);

    Optional<ChatMessage> findTopByChatRoom_RoomIdOrderBySentAtDesc(String roomId);
}
