package com.kmedical.control;

import com.kmedical.domain.entity.RoleChangeHistory;
import com.kmedical.domain.entity.UserRole;
import com.kmedical.domain.enums.UserRoleName;
import com.kmedical.dto.rbac.RoleChangeRequestDTO;
import com.kmedical.dto.rbac.UserRoleDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRV-C18 — RBACController
 * 책임: 역할(Role) 부여·회수, RoleChangeHistory 기록.
 * UC: UC-A13
 * 제약: 모든 역할 변경 시 RoleChangeHistory를 반드시 기록한다.
 */
public class RBACController {

    /** targetUserId → 현재 활성 역할명 */
    private final Map<String, UserRoleName> activeRoleStore = new HashMap<>();
    /** targetUserId → 변경 이력 목록 */
    private final Map<String, List<RoleChangeHistory>> historyStore = new HashMap<>();
    /** roleId → UserRole 정의 */
    private final Map<String, UserRole> roleDefStore = new HashMap<>();

    private void guardNotClosedDown() {
        if (SystemStateRegistry.getInstance().isClosedDown()) {
            throw new IllegalStateException("System is closed down. Customer operations are not permitted.");
        }
    }

    /**
     * 사용자에게 역할을 부여한다.
     * System Response: 중복 역할 확인 → 역할 저장 → RoleChangeHistory 기록
     */
    public UserRoleDTO assignRole(RoleChangeRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getTargetUserId() == null || request.getNewRole() == null) {
            throw new IllegalArgumentException("Role assignment request is incomplete.");
        }

        UserRoleName current = activeRoleStore.get(request.getTargetUserId());
        if (current == request.getNewRole()) {
            throw new IllegalStateException("User already has the role: " + request.getNewRole());
        }

        UserRoleName previous = activeRoleStore.put(request.getTargetUserId(), request.getNewRole());
        recordHistory(request.getActorId(), request.getTargetUserId(), previous, request.getNewRole());

        return buildDTO(request.getTargetUserId(), request.getNewRole());
    }

    /**
     * 사용자의 역할을 회수한다.
     * System Response: 역할 존재 확인 → 역할 제거 → RoleChangeHistory 기록
     */
    public UserRoleDTO revokeRole(RoleChangeRequestDTO request) {
        guardNotClosedDown();
        if (request == null || request.getTargetUserId() == null) {
            throw new IllegalArgumentException("Role revoke request is incomplete.");
        }

        UserRoleName current = activeRoleStore.remove(request.getTargetUserId());
        if (current == null) {
            throw new IllegalArgumentException("No active role found for user: " + request.getTargetUserId());
        }

        recordHistory(request.getActorId(), request.getTargetUserId(), current, null);
        return buildDTO(request.getTargetUserId(), null);
    }

    /**
     * 사용자의 현재 역할을 조회한다.
     */
    public List<UserRoleDTO> getRolesForUser(String userId) {
        guardNotClosedDown();
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
        historyStore.computeIfAbsent(targetUserId, k -> new ArrayList<>()).add(history);
    }

    private UserRoleDTO buildDTO(String userId, UserRoleName roleName) {
        UserRoleDTO dto = new UserRoleDTO();
        dto.setUserRoleId(UUID.randomUUID().toString());
        dto.setRoleName(roleName);
        return dto;
    }
}
