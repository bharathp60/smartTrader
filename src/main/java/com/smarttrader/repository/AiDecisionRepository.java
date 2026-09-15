package com.smarttrader.repository;

import com.smarttrader.entity.AiDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AiDecisionRepository extends JpaRepository<AiDecision, UUID> {
    List<AiDecision> findTop20ByOrderByCreatedAtDesc();
    List<AiDecision> findBySymbolOrderByCreatedAtDesc(String symbol);
}
