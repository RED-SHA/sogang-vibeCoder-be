package com.k.medtour.domain.admin.dto;

import com.k.medtour.domain.admin.entity.Member;

public record UserInfo(
        Long id,
        String email,
        String name,
        String role,
        boolean isNewUser
) {
    public static UserInfo from(Member member, boolean isNewUser) {
        return new UserInfo(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole().getName(),
                isNewUser
        );
    }
}
