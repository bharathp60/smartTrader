package com.smarttrader.screener;

import com.smarttrader.entity.Instrument;
import com.smarttrader.universe.StockUniverseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Computes and caches sector strength scores based on the relative price performance
 * of stocks within each sector during the current trading session.
 *
 * <p>Score range: 0.0 (weakest sector) to 1.0 (strongest sector).
 * All sectors start at a neutral {@code 0.5} on initialisation and are updated
 * during the trading session via {@link #updateSectorStrengths(Map)}.
 *
 * <p>Normalisation: a sector average change of {@code +3 %} maps to {@code 1.0};
 * {@code -3 %} maps to {@code 0.0}; values are clamped within {@code [0.0, 1.0]}.
 */
@Service
public class SectorAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(SectorAnalysisService.class);

    /** Default score used when sector data has not yet been updated. */
    private static final double NEUTRAL_STRENGTH = 0.5;

    /**
     * Daily normalisation range: ±3 % is mapped to the full [0, 1] range.
     * Adjust if typical daily sector swings are larger / smaller.
     */
    private static final double NORMALISATION_HALF_RANGE = 3.0;

    private final StockUniverseService stockUniverseService;

    /**
     * Thread-safe cache: sector name → strength score.
     * Populated lazily on first update; defaults are seeded in constructor.
     */
    private final ConcurrentHashMap<String, Double> sectorStrengthCache = new ConcurrentHashMap<>();

    public SectorAnalysisService(StockUniverseService stockUniverseService) {
        this.stockUniverseService = stockUniverseService;
        initDefaults();
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /**
     * Seeds the cache with {@link #NEUTRAL_STRENGTH} for every sector known at
     * startup.  Ensures {@link #getSectorStrength(String)} never returns a
     * missing-key fallback during the first trading cycle before any live
     * price-change data has arrived.
     */
    private void initDefaults() {
        List<String> sectors = stockUniverseService.getAllSectors();
        for (String sector : sectors) {
            sectorStrengthCache.put(sector, NEUTRAL_STRENGTH);
        }
        log.info("SectorAnalysisService initialised with {} sectors at neutral strength", sectors.size());
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Updates the sector strength cache based on intra-day price changes.
     *
     * <p>For each active instrument the method looks up the instrument's sector and
     * accumulates the price change.  The average change per sector is then
     * normalised to {@code [0.0, 1.0]}.
     *
     * @param priceChanges map of {@code symbol → percentage price change today}
     *                     (e.g. {@code "RELIANCE" → 1.5} means +1.5 %)
     */
    public void updateSectorStrengths(Map<String, Double> priceChanges) {
        if (priceChanges == null || priceChanges.isEmpty()) {
            log.debug("updateSectorStrengths called with empty price changes; cache unchanged");
            return;
        }

        List<Instrument> instruments = stockUniverseService.getActiveInstruments();
        Map<String, List<Double>> sectorChanges = new HashMap<>();

        for (Instrument inst : instruments) {
            String sector = inst.getSector();
            if (sector == null || sector.isBlank()) continue;

            Double change = priceChanges.get(inst.getSymbol());
            if (change == null) continue;

            sectorChanges.computeIfAbsent(sector, k -> new ArrayList<>()).add(change);
        }

        int updatedCount = 0;
        for (Map.Entry<String, List<Double>> entry : sectorChanges.entrySet()) {
            List<Double> changes = entry.getValue();
            double avgChange = changes.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            // Linear normalisation: avgChange ∈ [-halfRange, +halfRange] → [0, 1]
            double normalised = (avgChange + NORMALISATION_HALF_RANGE) / (2.0 * NORMALISATION_HALF_RANGE);
            normalised = Math.max(0.0, Math.min(1.0, normalised));

            sectorStrengthCache.put(entry.getKey(), normalised);
            updatedCount++;
        }

        log.debug("Sector strengths updated for {} sector(s) from {} symbol price changes",
                updatedCount, priceChanges.size());
    }

    // -------------------------------------------------------------------------
    // Queries — by sector name
    // -------------------------------------------------------------------------

    /**
     * Returns the strength score for the given sector name.
     *
     * @param sector sector name (e.g. "IT", "BANKING")
     * @return score in [0.0, 1.0]; {@link #NEUTRAL_STRENGTH} if unknown
     */
    public double getSectorStrength(String sector) {
        if (sector == null || sector.isBlank()) return NEUTRAL_STRENGTH;
        return sectorStrengthCache.getOrDefault(sector, NEUTRAL_STRENGTH);
    }

    /**
     * Returns a snapshot of all sector strength scores keyed by sector name.
     */
    public Map<String, Double> getAllSectorStrengths() {
        return new HashMap<>(sectorStrengthCache);
    }

    /**
     * Returns the top {@code n} sectors sorted by descending strength.
     *
     * @param n maximum number of sectors to return
     * @return ordered list of sector names (strongest first)
     */
    public List<String> getTopSectors(int n) {
        return sectorStrengthCache.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Queries — by symbol
    // -------------------------------------------------------------------------

    /**
     * Returns the sector strength for the sector that the given symbol belongs to.
     * Looks up the symbol in the active instrument universe to resolve its sector.
     *
     * @param symbol trading symbol
     * @return strength score in [0.0, 1.0]; {@link #NEUTRAL_STRENGTH} if symbol not found
     */
    public double getSymbolSectorStrength(String symbol) {
        return stockUniverseService.getActiveInstruments().stream()
                .filter(i -> symbol.equals(i.getSymbol()))
                .findFirst()
                .map(i -> getSectorStrength(i.getSector()))
                .orElse(NEUTRAL_STRENGTH);
    }

    /**
     * Returns a map of {@code symbol → sectorStrength} for every active instrument
     * in the universe.  Used by {@link com.smarttrader.features.FeatureEngineeringService}
     * to attach sector context to feature vectors.
     *
     * @return map of symbol → strength score in [0.0, 1.0]
     */
    public Map<String, Double> getSectorStrengthsForUniverse() {
        Map<String, Double> result = new HashMap<>();
        for (Instrument inst : stockUniverseService.getActiveInstruments()) {
            result.put(inst.getSymbol(), getSectorStrength(inst.getSector()));
        }
        return result;
    }
}
