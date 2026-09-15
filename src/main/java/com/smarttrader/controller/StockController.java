package com.smarttrader.controller;

import com.smarttrader.entity.Instrument;
import com.smarttrader.exception.EntityNotFoundException;
import com.smarttrader.repository.InstrumentRepository;
import com.smarttrader.universe.StockUniverseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockUniverseService stockUniverseService;
    private final InstrumentRepository instrumentRepository;

    public StockController(StockUniverseService stockUniverseService, InstrumentRepository instrumentRepository) {
        this.stockUniverseService = stockUniverseService;
        this.instrumentRepository = instrumentRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<List<Instrument>> getAllActiveInstruments() {
        return ResponseEntity.ok(stockUniverseService.getActiveInstruments());
    }

    @GetMapping("/{symbol}")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<List<Instrument>> getInstrumentBySymbol(@PathVariable String symbol) {
        // findBySymbolIn handles exact symbol lookup across exchanges
        List<Instrument> instruments = instrumentRepository.findBySymbolIn(List.of(symbol));
        if (instruments.isEmpty()) {
            throw new EntityNotFoundException("Instrument", symbol);
        }
        return ResponseEntity.ok(instruments);
    }

    @GetMapping("/sectors")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<List<String>> getAllSectors() {
        return ResponseEntity.ok(stockUniverseService.getAllSectors());
    }
}
