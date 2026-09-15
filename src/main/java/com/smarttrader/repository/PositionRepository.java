package com.smarttrader.repository;

import com.smarttrader.entity.Position;
import com.smarttrader.entity.PositionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {
    List<Position> findByStatus(PositionStatus status);
    List<Position> findBySymbolAndStatus(String symbol, PositionStatus status);
    Optional<Position> findTopBySymbolAndStatusOrderByOpenedAtDesc(String symbol, PositionStatus status);
}
