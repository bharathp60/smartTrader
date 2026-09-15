package com.smarttrader.controller;

import com.smarttrader.entity.TradingProfile;
import com.smarttrader.screener.ScreeningCriteria;
import com.smarttrader.screener.ScreeningResult;
import com.smarttrader.screener.StockScreeningService;
import com.smarttrader.universe.StockUniverseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/screener")
public class ScreenerController {

    private final StockScreeningService stockScreeningService;
    private final StockUniverseService stockUniverseService;

    public ScreenerController(StockScreeningService stockScreeningService,
                               StockUniverseService stockUniverseService) {
        this.stockScreeningService = stockScreeningService;
        this.stockUniverseService = stockUniverseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<List<ScreeningResult>> runScreener(
            @RequestParam(defaultValue = "SWING") String profile) {
        TradingProfile tradingProfile = TradingProfile.valueOf(profile.toUpperCase());
        ScreeningCriteria criteria = ScreeningCriteria.defaults(tradingProfile);
        List<ScreeningResult> results = stockScreeningService.screenUniverse(
            stockUniverseService.getActiveInstruments(),
            criteria,
            Map.of(),   // rsiValues — empty for now (populated by scheduler in live mode)
            Map.of(),   // relativeVolumes
            Map.of()    // mlScores
        );
        return ResponseEntity.ok(results);
    }

    @GetMapping("/criteria")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<ScreeningCriteria> getScreeningCriteria(
            @RequestParam(defaultValue = "SWING") String profile) {
        TradingProfile tradingProfile = TradingProfile.valueOf(profile.toUpperCase());
        return ResponseEntity.ok(ScreeningCriteria.defaults(tradingProfile));
    }
}
