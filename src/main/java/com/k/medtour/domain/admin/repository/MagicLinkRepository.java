package com.k.medtour.domain.admin.repository;

import com.k.medtour.domain.admin.entity.MagicLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface MagicLinkRepository extends JpaRepository<MagicLink, Long> {

    Optional<MagicLink> findByToken(UUID token);

    @Query("SELECT COUNT(ml) FROM MagicLink ml WHERE ml.targetEmail = :target AND ml.createdAt > :since")
    long countRecentByTargetEmail(@Param("target") String target, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(ml) FROM MagicLink ml WHERE ml.targetPhone = :target AND ml.createdAt > :since")
    long countRecentByTargetPhone(@Param("target") String target, @Param("since") LocalDateTime since);
}
