package com.smarttrader.repository;

import com.smarttrader.entity.MlPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MlPredictionRepository extends JpaRepository<MlPrediction, UUID> {
    List<MlPrediction> findBySymbolAndPredictedAtAfterOrderByPredictedAtDesc(String symbol, Instant after);
    Optional<MlPrediction> findTopBySymbolOrderByPredictedAtDesc(String symbol);
}
