package com.smarttrader.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttrader.ai.AiDecisionDto;
import com.smarttrader.marketdata.QuoteDto;
import com.smarttrader.ranking.CandidateScore;
import com.smarttrader.risk.RiskCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Publishes real-time trading events over WebSocket to connected clients.
 * Topics:
 *   /topic/quotes/{symbol}  — latest quote for a symbol
 *   /topic/quotes           — all quotes
 *   /topic/signals          — new trading signals
 *   /topic/ai/decisions     — AI decisions
 *   /topic/orders           — order updates
 *   /topic/risk/alerts      — risk engine rejections
 *   /topic/system           — system events
 */
@Service
public class TradingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TradingEventPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public TradingEventPublisher(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishQuote(QuoteDto quote) {
        try {
            TradingEvent event = new TradingEvent("QUOTE", quote.symbol(), quote, Instant.now());
            messagingTemplate.convertAndSend("/topic/quotes/" + quote.symbol(), event);
            messagingTemplate.convertAndSend("/topic/quotes", event);
        } catch (Exception e) {
            log.debug("Failed to publish quote for {}: {}", quote.symbol(), e.getMessage());
        }
    }

    public void publishSignal(String symbol, CandidateScore score) {
        try {
            TradingEvent event = new TradingEvent("SIGNAL", symbol, score, Instant.now());
            messagingTemplate.convertAndSend("/topic/signals", event);
        } catch (Exception e) {
            log.debug("Failed to publish signal for {}: {}", symbol, e.getMessage());
        }
    }

    public void publishAiDecision(AiDecisionDto decision) {
        try {
            TradingEvent event = new TradingEvent("AI_DECISION", decision.symbol(), decision, Instant.now());
            messagingTemplate.convertAndSend("/topic/ai/decisions", event);
        } catch (Exception e) {
            log.debug("Failed to publish AI decision for {}: {}", decision.symbol(), e.getMessage());
        }
    }

    public void publishOrderUpdate(String clientOrderId, String status) {
        try {
            TradingEvent event = new TradingEvent("ORDER_UPDATE", clientOrderId, status, Instant.now());
            messagingTemplate.convertAndSend("/topic/orders", event);
        } catch (Exception e) {
            log.debug("Failed to publish order update: {}", e.getMessage());
        }
    }

    public void publishRiskAlert(RiskCheckResult result) {
        try {
            TradingEvent event = new TradingEvent("RISK_ALERT", null, result, Instant.now());
            messagingTemplate.convertAndSend("/topic/risk/alerts", event);
        } catch (Exception e) {
            log.debug("Failed to publish risk alert: {}", e.getMessage());
        }
    }

    public void publishSystemEvent(String message, String severity) {
        try {
            TradingEvent event = new TradingEvent("SYSTEM", null,
                    java.util.Map.of("message", message, "severity", severity), Instant.now());
            messagingTemplate.convertAndSend("/topic/system", event);
        } catch (Exception e) {
            log.debug("Failed to publish system event: {}", e.getMessage());
        }
    }
}
