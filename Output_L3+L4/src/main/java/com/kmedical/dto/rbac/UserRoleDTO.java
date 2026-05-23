package com.kmedical.dto.rbac;

import com.kmedical.domain.enums.UserRoleName;

import java.util.List;

/** UserRole 도메인 복사 DTO — Interface 계층 노출용 */
public class UserRoleDTO {

    private String userRoleId;
    private UserRoleName roleName;
    private List<String> permissions;

    public UserRoleDTO() {}

    public String getUserRoleId() { return userRoleId; }
    public void setUserRoleId(String userRoleId) { this.userRoleId = userRoleId; }

    public UserRoleName getRoleName() { return roleName; }
    public void setRoleName(UserRoleName roleName) { this.roleName = roleName; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
