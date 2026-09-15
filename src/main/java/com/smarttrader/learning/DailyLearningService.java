package com.smarttrader.learning;

import com.smarttrader.entity.AdaptationProposal;
import com.smarttrader.entity.DailyLearningReport;
import com.smarttrader.entity.TradeClassification;
import com.smarttrader.entity.TradeMistake;
import com.smarttrader.entity.TradeResult;
import com.smarttrader.entity.AdaptationStatus;
import com.smarttrader.repository.AdaptationProposalRepository;
import com.smarttrader.repository.TradeMistakeRepository;
import com.smarttrader.repository.TradeResultRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyLearningService {

    private final TradeResultRepository tradeResultRepository;
    private final TradeMistakeRepository tradeMistakeRepository;
    private final AdaptationProposalRepository adaptationProposalRepository;
    private final TradeClassifier tradeClassifier;
    private final MistakeDetectionService mistakeDetectionService;

    public DailyLearningService(TradeResultRepository tradeResultRepository,
                                TradeMistakeRepository tradeMistakeRepository,
                                AdaptationProposalRepository adaptationProposalRepository,
                                TradeClassifier tradeClassifier,
                                MistakeDetectionService mistakeDetectionService) {
        this.tradeResultRepository = tradeResultRepository;
        this.tradeMistakeRepository = tradeMistakeRepository;
        this.adaptationProposalRepository = adaptationProposalRepository;
        this.tradeClassifier = tradeClassifier;
        this.mistakeDetectionService = mistakeDetectionService;
    }

    public void runDailyLearning(LocalDate date) {
        Instant from = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<TradeResult> dailyTrades = tradeResultRepository.findByClosedAtBetween(from, to);

        List<TradeAnalysis> analysisList = new ArrayList<>();
        List<MistakeTaxonomy> allMistakes = new ArrayList<>();

        for (TradeResult trade : dailyTrades) {
            BigDecimal pnl = trade.getPnl() != null ? trade.getPnl() : BigDecimal.ZERO;
            TradeClassification classification = tradeClassifier.classifyTrade(pnl, 0.8, 0.6, 2.0, false);
            
            TradeAnalysis analysis = new TradeAnalysis(
                trade.getSymbol(), classification, pnl, 
                trade.getReturnPct() != null ? trade.getReturnPct().doubleValue() : 0.0, 
                0.8, 0.6, null, new ArrayList<>(), new ArrayList<>(), false, false
            );
            
            analysisList.add(analysis);
            
            List<MistakeTaxonomy> mistakes = mistakeDetectionService.detectMistakes(analysis, 0.0, 0.0, 2.0, false, true);
            allMistakes.addAll(mistakes);
        }
        
        List<AdaptationProposal> proposals = generateAdaptationProposals(allMistakes);
        for(AdaptationProposal p : proposals) {
            if(p.getSampleSize() != null && p.getSampleSize() >= 10) {
                adaptationProposalRepository.save(p);
            }
        }
    }

    public List<AdaptationProposal> generateAdaptationProposals(List<MistakeTaxonomy> recurringMistakes) {
        Map<MistakeTaxonomy, Long> counts = recurringMistakes.stream()
                .collect(Collectors.groupingBy(m -> m, Collectors.counting()));
        
        List<AdaptationProposal> proposals = new ArrayList<>();
        counts.forEach((mistake, count) -> {
            if (count >= 2) {
                AdaptationProposal p = new AdaptationProposal();
                p.setProposalType("MITIGATE_" + mistake.name());
                p.setStatus(AdaptationStatus.PROPOSED);
                p.setReason("Recurring mistake: " + mistake.name());
                p.setSampleSize(count.intValue());
                proposals.add(p);
            }
        });
        return proposals;
    }

    public double computeWinRate(List<TradeAnalysis> analyses) {
        if (analyses.isEmpty()) return 0.0;
        long wins = analyses.stream().filter(a -> a.pnl().compareTo(BigDecimal.ZERO) > 0).count();
        return (double) wins / analyses.size();
    }

    public double computeProfitFactor(List<TradeAnalysis> analyses) {
        BigDecimal grossProfit = BigDecimal.ZERO;
        BigDecimal grossLoss = BigDecimal.ZERO;
        
        for (TradeAnalysis a : analyses) {
            if (a.pnl().compareTo(BigDecimal.ZERO) > 0) {
                grossProfit = grossProfit.add(a.pnl());
            } else {
                grossLoss = grossLoss.add(a.pnl().abs());
            }
        }
        
        if (grossLoss.compareTo(BigDecimal.ZERO) == 0) return grossProfit.compareTo(BigDecimal.ZERO) > 0 ? 99.9 : 0.0;
        return grossProfit.divide(grossLoss, 4, java.math.RoundingMode.HALF_UP).doubleValue();
    }
}
