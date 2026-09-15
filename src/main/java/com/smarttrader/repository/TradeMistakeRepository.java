package com.smarttrader.repository;

import com.smarttrader.entity.TradeMistake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TradeMistakeRepository extends JpaRepository<TradeMistake, UUID> {
    List<TradeMistake> findByTaxonomyOrderByDetectedAtDesc(String taxonomy);
    List<TradeMistake> findByDetectedAtAfter(Instant after);

    @Query("SELECT tm.taxonomy, COUNT(tm) FROM TradeMistake tm WHERE tm.detectedAt >= :since GROUP BY tm.taxonomy ORDER BY COUNT(tm) DESC")
    List<Object[]> findMostFrequentMistakesSince(@Param("since") Instant since);
}
