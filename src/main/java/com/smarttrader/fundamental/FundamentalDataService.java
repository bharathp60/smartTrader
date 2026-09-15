package com.smarttrader.fundamental;

import com.smarttrader.entity.FundamentalData;
import com.smarttrader.repository.FundamentalDataRepository;
import com.smarttrader.repository.InstrumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides fundamental quality scores for symbols used in trade filtering and ranking.
 *
 * <p>Score range: 0.0 (weakest) to 1.0 (strongest).  When real provider data is
 * unavailable the service returns a conservative neutral score of 0.5 so that
 * missing data never silently blocks a trade.
 *
 * <p>Production flow (TODO):
 * <ol>
 *   <li>A scheduled job fetches data from a fundamental provider (e.g. Screener.in, Ticker Tape).
 *   <li>Results are persisted as {@link FundamentalData} rows via {@link FundamentalDataRepository}.
 *   <li>{@link #getFundamentalScore(String)} reads the latest row and computes a weighted average
 *       of sub-scores returned by the individual {@code score*()} helpers.
 * </ol>
 */
@Service
public class FundamentalDataService {

    private static final Logger log = LoggerFactory.getLogger(FundamentalDataService.class);

    /** Neutral score returned when no fundamental data is available for a symbol. */
    private static final double NEUTRAL_SCORE = 0.5;

    private final InstrumentRepository instrumentRepository;
    private final FundamentalDataRepository fundamentalDataRepository;

    public FundamentalDataService(InstrumentRepository instrumentRepository,
                                   FundamentalDataRepository fundamentalDataRepository) {
        this.instrumentRepository = instrumentRepository;
        this.fundamentalDataRepository = fundamentalDataRepository;
    }

    // -------------------------------------------------------------------------
    // Primary API
    // -------------------------------------------------------------------------

    /**
     * Returns a fundamental quality score in the range [0.0, 1.0] for a symbol.
     *
     * <p>When the {@link FundamentalData} row is missing or
     * {@link FundamentalData#isDataAvailable()} is {@code false}, returns
     * {@value #NEUTRAL_SCORE} (do not penalise symbols with no data).
     *
     * <p>TODO: parse the JSON {@code metrics} column and compute:
     * <pre>
     *   score = 0.25 * scorePe(pe)
     *         + 0.20 * scoreEpsGrowth(epsGrowth)
     *         + 0.20 * scoreDebtEquity(debtToEquity)
     *         + 0.20 * scorePb(pb)
     *         + 0.15 * scoreRoce(roce)
     * </pre>
     */
    public double getFundamentalScore(String symbol) {
        Optional<FundamentalData> latest =
                fundamentalDataRepository.findFirstBySymbolOrderByReportedAtDesc(symbol);

        if (latest.isEmpty() || !latest.get().isDataAvailable()) {
            log.debug("No fundamental data for symbol {}; returning neutral score", symbol);
            return NEUTRAL_SCORE;
        }

        // TODO: deserialise latest.get().getMetrics() (JSONB string) and compute weighted score.
        // Returning NEUTRAL_SCORE until the metrics parser is implemented.
        return NEUTRAL_SCORE;
    }

    /**
     * Returns {@code true} when a symbol's fundamental score meets or exceeds
     * {@code minScore}.  Symbols without data are considered acceptable (returns
     * {@code true}) so that missing data does not silently block trades.
     *
     * @param symbol   trading symbol
     * @param minScore minimum acceptable score (0.0 – 1.0)
     */
    public boolean isFundamentallyAcceptable(String symbol, double minScore) {
        double score = getFundamentalScore(symbol);
        return score >= minScore;
    }

    /**
     * Returns a map of symbol → fundamental score for batch screening.
     */
    public Map<String, Double> getFundamentalScores(List<String> symbols) {
        Map<String, Double> scores = new HashMap<>();
        for (String sym : symbols) {
            scores.put(sym, getFundamentalScore(sym));
        }
        return scores;
    }

    // -------------------------------------------------------------------------
    // Sub-scorers (stateless helpers, exposed for unit testing)
    // -------------------------------------------------------------------------

    /**
     * P/E quality score: lower P/E is better.
     *
     * @param peRatio trailing price-to-earnings ratio
     * @return quality score in [0.1, 1.0]
     */
    public double scorePe(double peRatio) {
        if (peRatio <= 0) return 0.3;  // negative earnings — uncertain signal
        if (peRatio < 10) return 1.0;
        if (peRatio < 20) return 0.8;
        if (peRatio < 30) return 0.6;
        if (peRatio < 50) return 0.4;
        return 0.1; // very expensive
    }

    /**
     * EPS growth score: higher growth is better.
     *
     * @param epsGrowthPercent year-on-year EPS growth in percent
     * @return quality score in [0.1, 1.0]
     */
    public double scoreEpsGrowth(double epsGrowthPercent) {
        if (epsGrowthPercent > 30) return 1.0;
        if (epsGrowthPercent > 20) return 0.9;
        if (epsGrowthPercent > 10) return 0.7;
        if (epsGrowthPercent > 0)  return 0.5;
        return 0.1; // negative or zero growth
    }

    /**
     * Debt-to-equity score: lower leverage is safer.
     *
     * @param debtToEquity debt-to-equity ratio
     * @return quality score in [0.2, 1.0]
     */
    public double scoreDebtEquity(double debtToEquity) {
        if (debtToEquity < 0.3) return 1.0;
        if (debtToEquity < 0.7) return 0.8;
        if (debtToEquity < 1.5) return 0.5;
        return 0.2; // highly leveraged
    }
}
