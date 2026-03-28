package com.k.medtour.domain.admin.repository;

import com.k.medtour.domain.admin.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

    boolean existsByEmail(String email);

    @Query("SELECT m FROM Member m JOIN FETCH m.role WHERE m.id = :id")
    Optional<Member> findByIdWithRole(@Param("id") Long id);

    @Query("SELECT m FROM Member m JOIN FETCH m.role WHERE m.email = :email")
    Optional<Member> findByEmailWithRole(@Param("email") String email);

    @Query("SELECT m FROM Member m WHERE m.role.name = :roleName AND m.deletedAt IS NULL")
    Page<Member> findAllByRoleName(@Param("roleName") String roleName, Pageable pageable);
}
