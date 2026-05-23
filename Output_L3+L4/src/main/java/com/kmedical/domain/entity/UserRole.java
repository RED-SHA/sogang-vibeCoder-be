package com.kmedical.domain.entity;

import com.kmedical.domain.enums.UserRoleName;

import java.util.List;

/** C30 — UserRole «entity» */
public class UserRole {

    private String userRoleId;
    private UserRoleName roleName;
    private List<String> permissions;

    public UserRole() {}

    public String getUserRoleId() { return userRoleId; }
    public void setUserRoleId(String userRoleId) { this.userRoleId = userRoleId; }

    public UserRoleName getRoleName() { return roleName; }
    public void setRoleName(UserRoleName roleName) { this.roleName = roleName; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
