package com.smarttrader.repository;

import com.smarttrader.entity.RiskEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RiskEventRepository extends JpaRepository<RiskEvent, UUID> {
    List<RiskEvent> findByDecisionAndOccurredAtAfterOrderByOccurredAtDesc(String decision, Instant after);
    long countByDecisionAndOccurredAtAfter(String decision, Instant after);
}
