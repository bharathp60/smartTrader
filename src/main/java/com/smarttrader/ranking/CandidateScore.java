package com.smarttrader.ranking;

import com.smarttrader.entity.TradingProfile;
import java.time.Instant;

public record CandidateScore(
    String symbol,
    TradingProfile profile,
    double technicalScore,
    double momentumScore,
    double volumeScore,
    double sectorScore,
    double regimeScore,
    double fundamentalScore,
    double mlScore,
    double riskRewardScore,
    double liquidityScore,
    double aiContextScore,
    double totalScore,
    Instant calculatedAt
) {}
