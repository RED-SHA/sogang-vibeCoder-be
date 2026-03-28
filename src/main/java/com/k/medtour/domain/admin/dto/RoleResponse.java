package com.k.medtour.domain.admin.dto;

import com.k.medtour.domain.admin.entity.Permission;
import com.k.medtour.domain.admin.entity.Role;

import java.util.List;

public record RoleResponse(
        Long id,
        String name,
        String description,
        List<String> permissions
) {
    public static RoleResponse from(Role role) {
        List<String> permissionNames = role.getPermissions().stream()
                .map(Permission::getName)
                .sorted()
                .toList();
        return new RoleResponse(role.getId(), role.getName(), role.getDescription(), permissionNames);
    }
}
