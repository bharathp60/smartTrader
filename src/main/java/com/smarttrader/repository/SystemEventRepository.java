package com.smarttrader.repository;

import com.smarttrader.entity.SystemEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SystemEventRepository extends JpaRepository<SystemEvent, UUID> {
    List<SystemEvent> findTop50ByOrderByOccurredAtDesc();
    List<SystemEvent> findBySeverityOrderByOccurredAtDesc(String severity);
}
