package com.k.medtour.domain.admin.entity;

import com.k.medtour.domain.admin.enums.MagicLinkTargetType;
import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "magic_link")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MagicLink extends BaseEntity {

    @Column(name = "token", nullable = false, unique = true, updatable = false)
    private UUID token;

    @Column(name = "target_email")
    private String targetEmail;

    @Column(name = "target_phone", length = 30)
    private String targetPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 10)
    private MagicLinkTargetType targetType;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "language", nullable = false, length = 5)
    private String language = "en";

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public MagicLink(UUID token, String targetEmail, String targetPhone,
                     MagicLinkTargetType targetType, String role, String language,
                     LocalDate birthDate, LocalDateTime expiresAt, Member member) {
        this.token = token != null ? token : UUID.randomUUID();
        this.targetEmail = targetEmail;
        this.targetPhone = targetPhone;
        this.targetType = targetType;
        this.role = role;
        this.language = language != null ? language : "en";
        this.birthDate = birthDate;
        this.expiresAt = expiresAt;
        this.member = member;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isUsed() {
        return this.usedAt != null;
    }

    public void markAsUsed() {
        this.usedAt = LocalDateTime.now();
    }

    public void linkMember(Member member) {
        this.member = member;
    }
}
