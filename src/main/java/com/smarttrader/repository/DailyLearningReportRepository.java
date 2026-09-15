package com.smarttrader.repository;

import com.smarttrader.entity.DailyLearningReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyLearningReportRepository extends JpaRepository<DailyLearningReport, UUID> {
    Optional<DailyLearningReport> findByReportDate(LocalDate date);
    List<DailyLearningReport> findTop30ByOrderByReportDateDesc();
}
