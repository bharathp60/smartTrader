package com.smarttrader.controller;

import com.smarttrader.entity.AdaptationProposal;
import com.smarttrader.entity.DailyLearningReport;
import com.smarttrader.entity.TradeMistake;
import com.smarttrader.repository.AdaptationProposalRepository;
import com.smarttrader.repository.DailyLearningReportRepository;
import com.smarttrader.repository.TradeMistakeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/learning")
public class LearningController {

    private final DailyLearningReportRepository dailyLearningReportRepository;
    private final TradeMistakeRepository tradeMistakeRepository;
    private final AdaptationProposalRepository adaptationProposalRepository;

    public LearningController(
            DailyLearningReportRepository dailyLearningReportRepository,
            TradeMistakeRepository tradeMistakeRepository,
            AdaptationProposalRepository adaptationProposalRepository) {
        this.dailyLearningReportRepository = dailyLearningReportRepository;
        this.tradeMistakeRepository = tradeMistakeRepository;
        this.adaptationProposalRepository = adaptationProposalRepository;
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<DailyLearningReport> getTodayLearningReport() {
        return dailyLearningReportRepository.findByReportDate(LocalDate.now())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/mistakes")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<List<TradeMistake>> getRecentMistakes() {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        return ResponseEntity.ok(tradeMistakeRepository.findByDetectedAtAfter(since));
    }

    @GetMapping("/adaptations")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<List<AdaptationProposal>> getAdaptations() {
        return ResponseEntity.ok(adaptationProposalRepository.findAll());
    }
}
