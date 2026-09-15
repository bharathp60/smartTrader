package com.smarttrader.screener;

import com.smarttrader.entity.Instrument;
import com.smarttrader.universe.StockUniverseService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockScreeningService {

    private final StockUniverseService stockUniverseService;

    public StockScreeningService(StockUniverseService stockUniverseService) {
        this.stockUniverseService = stockUniverseService;
    }

    public List<ScreeningResult> screenUniverse(List<Instrument> instruments, ScreeningCriteria criteria, Map<String, Double> rsiValues, Map<String, Double> relativeVolumes, Map<String, Double> mlScores) {
        return instruments.stream().map(inst -> {
            String sym = inst.getSymbol();
            boolean passedLiq = inst.getAverageDailyVolume() != null && inst.getAverageDailyVolume().compareTo(criteria.minAvgVolume()) >= 0;
            boolean passedVol = relativeVolumes.containsKey(sym) && relativeVolumes.get(sym) >= criteria.minRelativeVolume();
            boolean passedTech = rsiValues.containsKey(sym) && rsiValues.get(sym) >= criteria.minRsiValue() && rsiValues.get(sym) <= criteria.maxRsiValue();
            boolean passedMl = mlScores.containsKey(sym) && mlScores.get(sym) >= criteria.minMlProbability();
            
            Map<String, Object> details = new HashMap<>();
            details.put("volume", inst.getAverageDailyVolume());
            if (rsiValues.containsKey(sym)) details.put("rsi", rsiValues.get(sym));
            if (relativeVolumes.containsKey(sym)) details.put("relativeVolume", relativeVolumes.get(sym));
            if (mlScores.containsKey(sym)) details.put("mlScore", mlScores.get(sym));

            return new ScreeningResult(sym, passedLiq, passedVol, passedTech, passedMl, details);
        }).toList();
    }

    public List<Instrument> filterByLiquidity(List<Instrument> instruments, BigDecimal minAvgVolume) {
        return instruments.stream()
                .filter(i -> i.getAverageDailyVolume() != null && i.getAverageDailyVolume().compareTo(minAvgVolume) >= 0)
                .toList();
    }

    public List<Instrument> filterByVolume(List<Instrument> instruments, double minRelativeVolume, Map<String, Double> relativeVolumes) {
        return instruments.stream()
                .filter(i -> relativeVolumes.containsKey(i.getSymbol()) && relativeVolumes.get(i.getSymbol()) >= minRelativeVolume)
                .toList();
    }

    public List<Instrument> filterByTechnicals(List<Instrument> instruments, Map<String, Double> rsiValues, ScreeningCriteria criteria) {
        return instruments.stream()
                .filter(i -> {
                    Double rsi = rsiValues.get(i.getSymbol());
                    return rsi != null && rsi >= criteria.minRsiValue() && rsi <= criteria.maxRsiValue();
                })
                .toList();
    }

    public List<Instrument> filterByMlScore(List<Instrument> instruments, Map<String, Double> mlScores, double minScore) {
        return instruments.stream()
                .filter(i -> mlScores.containsKey(i.getSymbol()) && mlScores.get(i.getSymbol()) >= minScore)
                .toList();
    }
}
