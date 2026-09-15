package com.smarttrader.repository;

import com.smarttrader.entity.TradeResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeResultRepository extends JpaRepository<TradeResult, UUID> {
    List<TradeResult> findByClosedAtBetween(Instant from, Instant to);
    List<TradeResult> findBySymbol(String symbol);

    @Query("SELECT SUM(tr.pnl) FROM TradeResult tr WHERE tr.closedAt >= :from")
    Optional<BigDecimal> sumPnlSince(@Param("from") Instant from);
}
