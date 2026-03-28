package com.k.medtour.domain.admin.repository;

import com.k.medtour.domain.admin.entity.MemberConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberConsentRepository extends JpaRepository<MemberConsent, Long> {

    Optional<MemberConsent> findTopByMemberIdOrderByConsentedAtDesc(Long memberId);
}
