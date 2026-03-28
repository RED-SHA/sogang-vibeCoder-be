package com.k.medtour.domain.proposal.repository;

import com.k.medtour.domain.proposal.entity.ProposalItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalItemRepository extends JpaRepository<ProposalItem, Long> {

    List<ProposalItem> findByProposalId(Long proposalId);
}
