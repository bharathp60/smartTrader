package com.smarttrader.learning;

import com.smarttrader.entity.TradeClassification;
import com.smarttrader.regime.MarketRegime;

import java.math.BigDecimal;
import java.util.List;

public record TradeAnalysis(
    String symbol,
    TradeClassification classification,
    BigDecimal pnl,
    double returnPct,
    double aiConfidence,
    double mlProbability,
    MarketRegime regimeAtEntry,
    List<MistakeTaxonomy> mistakes,
    List<String> insights,
    boolean wasStopTriggered,
    boolean wasTargetHit
) {}
