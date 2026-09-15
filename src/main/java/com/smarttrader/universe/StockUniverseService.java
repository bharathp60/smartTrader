package com.smarttrader.universe;

import com.smarttrader.entity.Instrument;
import com.smarttrader.entity.InstrumentStatus;
import com.smarttrader.repository.InstrumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StockUniverseService {
    
    private static final Logger log = LoggerFactory.getLogger(StockUniverseService.class);
    
    private final InstrumentRepository instrumentRepository;

    public StockUniverseService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public List<Instrument> getActiveInstruments() {
        return instrumentRepository.findByStatus(InstrumentStatus.ACTIVE);
    }

    public List<Instrument> getLiquidInstruments(BigDecimal minAvgVolume) {
        return getActiveInstruments().stream()
                .filter(i -> i.getAverageDailyVolume() != null && i.getAverageDailyVolume().compareTo(minAvgVolume) >= 0)
                .toList();
    }

    public List<Instrument> getInstrumentsByStatus(InstrumentStatus status) {
        return instrumentRepository.findByStatus(status);
    }

    public List<Instrument> getInstrumentsBySector(String sector) {
        return instrumentRepository.findByStatusAndSector(InstrumentStatus.ACTIVE, sector);
    }

    public long getUniverseSize() {
        return getActiveInstruments().size();
    }

    public List<String> getAllSectors() {
        return instrumentRepository.findDistinctActiveSectors(InstrumentStatus.ACTIVE);
    }

    public void updateUniverseFromProvider() {
        log.info("Universe update: provider integration pending");
    }
}
