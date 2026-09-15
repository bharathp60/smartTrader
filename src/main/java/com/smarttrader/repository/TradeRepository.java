package com.smarttrader.repository;

import com.smarttrader.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findBySymbolOrderByExecutedAtDesc(String symbol);
    List<Trade> findByExecutedAtAfterOrderByExecutedAtDesc(Instant after);
}
