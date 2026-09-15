package com.smarttrader.repository;

import com.smarttrader.entity.Instrument;
import com.smarttrader.entity.InstrumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, UUID> {
    List<Instrument> findByStatus(InstrumentStatus status);
    List<Instrument> findByStatusAndSector(InstrumentStatus status, String sector);
    Optional<Instrument> findBySymbolAndExchange(String symbol, String exchange);
    List<Instrument> findBySymbolIn(Collection<String> symbols);
    
    @Query("SELECT DISTINCT i.sector FROM Instrument i WHERE i.status = :status AND i.sector IS NOT NULL")
    List<String> findDistinctActiveSectors(@Param("status") InstrumentStatus status);
}
