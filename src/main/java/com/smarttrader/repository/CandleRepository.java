package com.smarttrader.repository;

import com.smarttrader.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandleRepository extends JpaRepository<Candle, UUID> {
    List<Candle> findBySymbolAndTimeframeAndOpenedAtBetweenOrderByOpenedAtAsc(
        String symbol, String timeframe, Instant from, Instant to);
        
    Optional<Candle> findTopBySymbolAndTimeframeOrderByOpenedAtDesc(String symbol, String timeframe);
    
    long countBySymbolAndTimeframe(String symbol, String timeframe);
}
