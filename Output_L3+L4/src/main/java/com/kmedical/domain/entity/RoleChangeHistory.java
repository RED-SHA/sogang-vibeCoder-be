package com.kmedical.domain.entity;

import com.kmedical.domain.enums.UserRoleName;

import java.time.LocalDateTime;

/** C31 — RoleChangeHistory «entity» (UserRole 변경 시 기록 필수) */
public class RoleChangeHistory {

    private String roleChangeHistoryId;
    private String actorId;
    private String targetUserId;
    private UserRoleName previousRole;
    private UserRoleName newRole;
    private LocalDateTime changedAt;

    public RoleChangeHistory() {}

    public String getRoleChangeHistoryId() { return roleChangeHistoryId; }
    public void setRoleChangeHistoryId(String roleChangeHistoryId) { this.roleChangeHistoryId = roleChangeHistoryId; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getTargetUserId() { return targetUserId; }
    public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }

    public UserRoleName getPreviousRole() { return previousRole; }
    public void setPreviousRole(UserRoleName previousRole) { this.previousRole = previousRole; }

    public UserRoleName getNewRole() { return newRole; }
    public void setNewRole(UserRoleName newRole) { this.newRole = newRole; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
