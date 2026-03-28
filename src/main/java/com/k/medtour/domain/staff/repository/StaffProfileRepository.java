package com.k.medtour.domain.staff.repository;

import com.k.medtour.domain.staff.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {

    @Query("SELECT sp FROM StaffProfile sp JOIN FETCH sp.member WHERE sp.member.id = :memberId")
    Optional<StaffProfile> findByMemberIdWithMember(@Param("memberId") Long memberId);

    Optional<StaffProfile> findByMemberId(Long memberId);
}
