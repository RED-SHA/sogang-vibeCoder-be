package com.kmedical.domain.entity;

import com.kmedical.domain.enums.Language;
import com.kmedical.domain.enums.OAuthProviderType;

import java.time.LocalDateTime;

/** C01 — User «entity» abstract */
public abstract class User {

    private String userId;
    private String email;
    private OAuthProviderType oauthProvider;
    private String oauthSubjectId;
    private Language preferredLanguage;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    protected User() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public OAuthProviderType getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(OAuthProviderType oauthProvider) { this.oauthProvider = oauthProvider; }

    public String getOauthSubjectId() { return oauthSubjectId; }
    public void setOauthSubjectId(String oauthSubjectId) { this.oauthSubjectId = oauthSubjectId; }

    public Language getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(Language preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
