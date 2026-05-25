package com.kmedical.control;

import com.kmedical.domain.entity.RoleChangeHistory;
import com.kmedical.domain.entity.UserRole;
import com.kmedical.domain.enums.UserRoleName;
import com.kmedical.dto.rbac.RoleChangeRequestDTO;
import com.kmedical.dto.rbac.UserRoleDTO;
import com.kmedical.util.AuditLogger;
import com.kmedical.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C18 — RBACController
 * 책임: 역할(Role) 부여·회수, RoleChangeHistory 기록.
 * UC: UC-A13
 * NFR 적용: ConcurrentHashMap, AuditLogger(ROLE_ASSIGNED/REVOKED)
 */
public class RBACController {

    private final Map<String, UserRoleName>              activeRoleStore = new ConcurrentHashMap<>();
    private final Map<String, List<RoleChangeHistory>>  historyStore    = new ConcurrentHashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            AuditLogger.closedDownAccess("RBACController", "UNKNOWN");
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 사용자에게 역할을 부여한다.
     * NFR-LOG: ROLE_ASSIGNED 감사 로그
     */
    public UserRoleDTO assignRole(RoleChangeRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "RoleChangeRequestDTO");
        ValidationUtil.requireNotBlank(request.getTargetUserId(), "targetUserId");
        ValidationUtil.requireNotNull(request.getNewRole(), "newRole");

        try {
            UserRoleName current = activeRoleStore.get(request.getTargetUserId());
            if (current == request.getNewRole())
                throw new IllegalStateException("User already has the role: " + request.getNewRole());

            UserRoleName previous = activeRoleStore.put(request.getTargetUserId(), request.getNewRole());
            recordHistory(request.getActorId(), request.getTargetUserId(), previous, request.getNewRole());

            AuditLogger.log("ROLE_ASSIGNED", request.getActorId(), request.getTargetUserId(), true,
                    "role=" + request.getNewRole() + " previous=" + previous);
            return buildDTO(request.getTargetUserId(), request.getNewRole());

        } catch (Exception e) {
            AuditLogger.log("ROLE_ASSIGNED", request.getActorId(), request.getTargetUserId(), false, e.getMessage());
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) throw e;
            throw new IllegalStateException("Role assignment failed: " + e.getMessage());
        }
    }

    /**
     * 사용자의 역할을 회수한다.
     * NFR-LOG: ROLE_REVOKED 감사 로그
     */
    public UserRoleDTO revokeRole(RoleChangeRequestDTO request) {
        guardNotClosedDown();
        ValidationUtil.requireNotNull(request, "RoleChangeRequestDTO");
        ValidationUtil.requireNotBlank(request.getTargetUserId(), "targetUserId");

        try {
            UserRoleName current = activeRoleStore.remove(request.getTargetUserId());
            if (current == null)
                throw new IllegalArgumentException("No active role found for user: " + request.getTargetUserId());

            recordHistory(request.getActorId(), request.getTargetUserId(), current, null);

            AuditLogger.log("ROLE_REVOKED", request.getActorId(), request.getTargetUserId(), true,
                    "revokedRole=" + current);
            return buildDTO(request.getTargetUserId(), null);

        } catch (Exception e) {
            AuditLogger.log("ROLE_REVOKED", request.getActorId(), request.getTargetUserId(), false, e.getMessage());
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) throw e;
            throw new IllegalStateException("Role revocation failed: " + e.getMessage());
        }
    }

    /**
     * 사용자의 현재 역할을 조회한다.
     */
    public List<UserRoleDTO> getRolesForUser(String userId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(userId, "userId");
        List<UserRoleDTO> result = new ArrayList<>();
        UserRoleName role = activeRoleStore.get(userId);
        if (role != null) result.add(buildDTO(userId, role));
        return result;
    }

    /**
     * 사용자의 역할 변경 이력을 조회한다.
     */
    public List<RoleChangeHistory> getChangeHistory(String userId) {
        guardNotClosedDown();
        ValidationUtil.requireNotBlank(userId, "userId");
        return new ArrayList<>(historyStore.getOrDefault(userId, new ArrayList<>()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void recordHistory(String actorId, String targetUserId,
                                UserRoleName previousRole, UserRoleName newRole) {
        RoleChangeHistory history = new RoleChangeHistory();
        history.setRoleChangeHistoryId(UUID.randomUUID().toString());
        history.setActorId(actorId);
        history.setTargetUserId(targetUserId);
        history.setPreviousRole(previousRole);
        history.setNewRole(newRole);
        history.setChangedAt(LocalDateTime.now());
        historyStore.computeIfAbsent(targetUserId, k -> new CopyOnWriteArrayList<>()).add(history);
    }

    private UserRoleDTO buildDTO(String userId, UserRoleName roleName) {
        UserRoleDTO dto = new UserRoleDTO();
        dto.setUserRoleId(UUID.randomUUID().toString());
        dto.setRoleName(roleName);
        return dto;
    }
}
