package com.kmedical.dto.rbac;

import com.kmedical.domain.enums.UserRoleName;

/** Interface → RBACController 간 역할 변경 요청 DTO */
public class RoleChangeRequestDTO {

    private String actorId;
    private String targetUserId;
    private UserRoleName newRole;

    public RoleChangeRequestDTO() {}

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getTargetUserId() { return targetUserId; }
    public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }

    public UserRoleName getNewRole() { return newRole; }
    public void setNewRole(UserRoleName newRole) { this.newRole = newRole; }
}
