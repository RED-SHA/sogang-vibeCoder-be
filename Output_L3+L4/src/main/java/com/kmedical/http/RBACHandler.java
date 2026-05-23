package com.kmedical.http;

import com.kmedical.control.RBACController;
import com.kmedical.domain.entity.RoleChangeHistory;
import com.kmedical.domain.enums.UserRoleName;
import com.kmedical.dto.rbac.RoleChangeRequestDTO;
import com.kmedical.dto.rbac.UserRoleDTO;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * SRV-C18 RBACController HTTP 매핑
 *
 * POST /api/rbac/assign           역할 부여
 * POST /api/rbac/revoke           역할 회수
 * GET  /api/rbac/{userId}/roles   현재 역할 조회
 * GET  /api/rbac/{userId}/history 역할 변경 이력 조회
 */
public class RBACHandler extends BaseHandler {

    private final RBACController rbacController;

    public RBACHandler(RBACController rbacController) {
        this.rbacController = rbacController;
    }

    @Override
    protected void dispatch(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String[] parts = path.split("/");

        if ("POST".equals(method) && "/api/rbac/assign".equals(path)) {
            handleAssign(ex);
        } else if ("POST".equals(method) && "/api/rbac/revoke".equals(path)) {
            handleRevoke(ex);
        } else if ("GET".equals(method) && parts.length == 5 && "roles".equals(parts[4])) {
            handleGetRoles(ex, parts[3]);
        } else if ("GET".equals(method) && parts.length == 5 && "history".equals(parts[4])) {
            handleGetHistory(ex, parts[3]);
        } else {
            sendError(ex, 404, "Not Found: " + method + " " + path);
        }
    }

    private void handleAssign(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        RoleChangeRequestDTO req = new RoleChangeRequestDTO();
        req.setActorId(body.get("actorId"));
        req.setTargetUserId(body.get("targetUserId"));
        if (body.get("newRole") != null) req.setNewRole(UserRoleName.valueOf(body.get("newRole")));
        sendJson(ex, 200, roleToMap(rbacController.assignRole(req)));
    }

    private void handleRevoke(HttpExchange ex) throws IOException {
        Map<String, String> body = JsonUtil.parse(readBody(ex));
        RoleChangeRequestDTO req = new RoleChangeRequestDTO();
        req.setActorId(body.get("actorId"));
        req.setTargetUserId(body.get("targetUserId"));
        sendJson(ex, 200, roleToMap(rbacController.revokeRole(req)));
    }

    private void handleGetRoles(HttpExchange ex, String userId) throws IOException {
        List<UserRoleDTO> list = rbacController.getRolesForUser(userId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (UserRoleDTO r : list) items.add(roleToMap(r));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private void handleGetHistory(HttpExchange ex, String userId) throws IOException {
        List<RoleChangeHistory> list = rbacController.getChangeHistory(userId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (RoleChangeHistory h : list) items.add(historyToMap(h));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", items.size()); resp.put("items", items);
        sendJson(ex, 200, resp);
    }

    private Map<String, Object> roleToMap(UserRoleDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userRoleId", dto.getUserRoleId());
        m.put("roleName",   dto.getRoleName() != null ? dto.getRoleName().name() : null);
        return m;
    }

    private Map<String, Object> historyToMap(RoleChangeHistory h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("roleChangeHistoryId", h.getRoleChangeHistoryId());
        m.put("actorId",             h.getActorId());
        m.put("targetUserId",        h.getTargetUserId());
        m.put("previousRole",        h.getPreviousRole() != null ? h.getPreviousRole().name() : null);
        m.put("newRole",             h.getNewRole() != null ? h.getNewRole().name() : null);
        m.put("changedAt",           h.getChangedAt() != null ? h.getChangedAt().toString() : null);
        return m;
    }
}
