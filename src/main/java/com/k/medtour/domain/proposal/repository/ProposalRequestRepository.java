package com.k.medtour.domain.proposal.repository;

import com.k.medtour.domain.proposal.entity.ProposalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalRequestRepository extends JpaRepository<ProposalRequest, Long> {

    List<ProposalRequest> findByPatientId(Long patientId);
}
