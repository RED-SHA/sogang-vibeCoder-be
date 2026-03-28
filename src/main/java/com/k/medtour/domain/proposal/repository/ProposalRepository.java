package com.k.medtour.domain.proposal.repository;

import com.k.medtour.domain.proposal.entity.Proposal;
import com.k.medtour.domain.proposal.enums.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    Page<Proposal> findByPatientId(Long patientId, Pageable pageable);

    Page<Proposal> findByStatus(ProposalStatus status, Pageable pageable);

    @Query("SELECT p FROM Proposal p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:patientId IS NULL OR p.patientId = :patientId) AND " +
            "p.deletedAt IS NULL")
    Page<Proposal> findAllWithFilters(
            @Param("status") ProposalStatus status,
            @Param("patientId") Long patientId,
            Pageable pageable);

    @Query("SELECT p FROM Proposal p WHERE p.patientId = :patientId AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "p.deletedAt IS NULL")
    Page<Proposal> findByPatientIdWithFilter(
            @Param("patientId") Long patientId,
            @Param("status") ProposalStatus status,
            Pageable pageable);
}
