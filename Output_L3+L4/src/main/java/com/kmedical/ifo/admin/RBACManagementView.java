package com.kmedical.ifo.admin;

import com.kmedical.control.RBACController;
import com.kmedical.domain.entity.RoleChangeHistory;
import com.kmedical.dto.rbac.RoleChangeRequestDTO;
import com.kmedical.dto.rbac.UserRoleDTO;

import java.util.List;

/**
 * IFO-A14 — RBACManagementView
 * UC: UC-A13 (역할 기반 접근 제어 관리)
 * 책임: 관리자가 사용자 역할을 부여·회수하고 변경 이력을 조회하는 UI 진입점.
 */
public class RBACManagementView {

    private final RBACController rbacController;

    public RBACManagementView(RBACController rbacController) {
        this.rbacController = rbacController;
    }

    /**
     * 관리자가 사용자에게 역할을 부여한다.
     * Actor Action: Admin assigns a role to a user.
     */
    public UserRoleDTO assignRole(RoleChangeRequestDTO request) {
        return rbacController.assignRole(request);
    }

    /**
     * 관리자가 사용자로부터 역할을 회수한다.
     * Actor Action: Admin revokes a role from a user.
     */
    public UserRoleDTO revokeRole(RoleChangeRequestDTO request) {
        return rbacController.revokeRole(request);
    }

    /**
     * 관리자가 사용자의 현재 역할 목록을 조회한다.
     * Actor Action: Admin views current roles of a user.
     */
    public List<UserRoleDTO> viewUserRoles(String userId) {
        return rbacController.getRolesForUser(userId);
    }

    /**
     * 관리자가 역할 변경 이력을 조회한다.
     * Actor Action: Admin audits role change history for a user.
     */
    public List<RoleChangeHistory> viewChangeHistory(String userId) {
        return rbacController.getChangeHistory(userId);
    }
}
