package com.k.medtour.domain.chat.repository;

import com.k.medtour.domain.chat.entity.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    boolean existsByChatRoom_RoomIdAndMemberId(String roomId, Long memberId);

    Optional<ChatRoomParticipant> findByChatRoom_RoomIdAndMemberId(String roomId, Long memberId);

    List<ChatRoomParticipant> findAllByChatRoom_RoomId(String roomId);

    @Query("SELECT p.memberId FROM ChatRoomParticipant p WHERE p.chatRoom.roomId = :roomId")
    List<Long> findMemberIdsByRoomId(@Param("roomId") String roomId);
}
