package com.k.medtour.domain.chat.repository;

import com.k.medtour.domain.chat.entity.ChatRoom;
import com.k.medtour.domain.chat.enums.ChatRoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query("SELECT cr FROM ChatRoom cr " +
            "JOIN cr.participants p " +
            "WHERE p.memberId = :memberId " +
            "ORDER BY cr.updatedAt DESC")
    Page<ChatRoom> findAllByParticipantMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr " +
            "JOIN cr.participants p " +
            "WHERE p.memberId = :memberId AND cr.type = :type " +
            "ORDER BY cr.updatedAt DESC")
    Page<ChatRoom> findAllByParticipantMemberIdAndType(
            @Param("memberId") Long memberId,
            @Param("type") ChatRoomType type,
            Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr " +
            "LEFT JOIN FETCH cr.participants " +
            "WHERE cr.roomId = :roomId")
    Optional<ChatRoom> findByIdWithParticipants(@Param("roomId") String roomId);

    boolean existsByRoomId(String roomId);

    Page<ChatRoom> findAllByType(ChatRoomType type, Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr ORDER BY cr.updatedAt DESC")
    Page<ChatRoom> findAllOrderByUpdatedAtDesc(Pageable pageable);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type = :type ORDER BY cr.updatedAt DESC")
    Page<ChatRoom> findAllByTypeOrderByUpdatedAtDesc(@Param("type") ChatRoomType type, Pageable pageable);
}
