package com.k.medtour.domain.notification.repository;

import com.k.medtour.domain.notification.entity.Notification;
import com.k.medtour.domain.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long memberId, Pageable pageable);

    long countByMemberIdAndIsReadFalseAndDeletedAtIsNull(Long memberId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.memberId = :memberId AND n.isRead = false")
    int markAllAsReadByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT n FROM Notification n WHERE n.type IN :types " +
            "AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findByTypeInOrderByCreatedAtDesc(
            @Param("types") List<NotificationType> types, Pageable pageable);
}
