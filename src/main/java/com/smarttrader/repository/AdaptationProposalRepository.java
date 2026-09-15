package com.smarttrader.repository;

import com.smarttrader.entity.AdaptationProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdaptationProposalRepository extends JpaRepository<AdaptationProposal, UUID> {
    List<AdaptationProposal> findByStatus(String status);
    List<AdaptationProposal> findByStatusOrderByCreatedAtDesc(String status);
}
