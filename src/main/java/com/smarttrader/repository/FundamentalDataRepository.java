package com.smarttrader.repository;

import com.smarttrader.entity.FundamentalData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FundamentalDataRepository extends JpaRepository<FundamentalData, UUID> {

    /**
     * Returns the most recent fundamental record for a symbol.
     */
    Optional<FundamentalData> findFirstBySymbolOrderByReportedAtDesc(String symbol);

    /**
     * Batch lookup for screening.
     */
    List<FundamentalData> findBySymbolIn(List<String> symbols);

    /**
     * Returns all records where data has been populated by a provider.
     */
    List<FundamentalData> findByDataAvailableTrue();
}
