package com.smarttrader.repository;

import com.smarttrader.entity.DailyPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyPerformanceRepository extends JpaRepository<DailyPerformance, UUID> {
    Optional<DailyPerformance> findByPerformanceDate(LocalDate date);
    List<DailyPerformance> findByPerformanceDateBetweenOrderByPerformanceDateAsc(LocalDate from, LocalDate to);
}
