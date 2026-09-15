package com.smarttrader.controller;

import com.smarttrader.broker.AccountInfo;
import com.smarttrader.broker.BrokerClient;
import com.smarttrader.entity.Position;
import com.smarttrader.entity.PositionStatus;
import com.smarttrader.exception.EntityNotFoundException;
import com.smarttrader.repository.PositionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PositionController {

    private final PositionRepository positionRepository;
    private final BrokerClient brokerClient;

    public PositionController(PositionRepository positionRepository, BrokerClient brokerClient) {
        this.positionRepository = positionRepository;
        this.brokerClient = brokerClient;
    }

    @GetMapping("/positions")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<List<Position>> getOpenPositions() {
        return ResponseEntity.ok(positionRepository.findByStatus(PositionStatus.OPEN));
    }

    @GetMapping("/positions/{id}")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<Position> getPosition(@PathVariable UUID id) {
        return positionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Position", id.toString()));
    }

    @GetMapping("/portfolio")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getPortfolioSummary() {
        AccountInfo account = brokerClient.getAccountInfo();
        List<Position> openPositions = positionRepository.findByStatus(PositionStatus.OPEN);

        return ResponseEntity.ok(Map.of(
            "totalCapital", account.totalCapital(),
            "availableCapital", account.availableCapital(),
            "usedMargin", account.usedMargin(),
            "unrealizedPnl", account.unrealizedPnl(),
            "currency", account.currency(),
            "openPositionsCount", openPositions.size()
        ));
    }
}
