package com.k.medtour.domain.admin.entity;

import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "oauth_provider", length = 20)
    private String oauthProvider;

    @Column(name = "oauth_id")
    private String oauthId;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "language", nullable = false, length = 5)
    private String language = "en";

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Builder
    public Member(String email, String name, Role role, String oauthProvider,
                  String oauthId, String phone, String language, String profileImage) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.phone = phone;
        this.language = language != null ? language : "en";
        this.profileImage = profileImage;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void updateProfile(String name, String phone, String language, String profileImage) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (language != null) this.language = language;
        if (profileImage != null) this.profileImage = profileImage;
    }
}
