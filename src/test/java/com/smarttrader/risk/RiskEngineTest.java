package com.smarttrader.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttrader.entity.OrderSide;
import com.smarttrader.repository.RiskEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.time.Duration;

class RiskEngineTest {

    private RiskEngine riskEngine;

    @Mock
    private RiskEventRepository riskEventRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    private RiskParameters defaultParams;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
        riskEngine = new RiskEngine(riskEventRepository, redisTemplate, objectMapper);
        defaultParams = RiskParameters.defaults();
    }

    private OrderRiskRequest buildRequest() {
        return new OrderRiskRequest(
            "AAPL", OrderSide.BUY, new BigDecimal("10"), new BigDecimal("150"), new BigDecimal("140"), new BigDecimal("170"),
            new BigDecimal("100000"), BigDecimal.ZERO, 0, 0, Map.of(), 2.0, 1_000_000, true, "PAPER"
        );
    }

    @Test
    void testMarketClosedRejection() {
        OrderRiskRequest request = new OrderRiskRequest("AAPL", OrderSide.BUY, BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(90), BigDecimal.valueOf(120), BigDecimal.valueOf(10000), BigDecimal.ZERO, 0, 0, Map.of(), 1.0, 1000000, false, "PAPER");
        RiskCheckResult result = riskEngine.checkOrder(request, defaultParams);
        assertFalse(result.isApproved());
        assertEquals("Market is closed", result.reason());
    }

    @Test
    void testDailyLossLimitRejection() {
        OrderRiskRequest request = new OrderRiskRequest("AAPL", OrderSide.BUY, BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(90), BigDecimal.valueOf(120), BigDecimal.valueOf(100000), BigDecimal.valueOf(3000), 0, 0, Map.of(), 1.0, 1000000, true, "PAPER");
        RiskCheckResult result = riskEngine.checkOrder(request, defaultParams);
        assertFalse(result.isApproved());
        assertEquals("Daily loss limit reached", result.reason());
    }

    @Test
    void testPositionCountRejection() {
        OrderRiskRequest request = new OrderRiskRequest("AAPL", OrderSide.BUY, BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(90), BigDecimal.valueOf(120), BigDecimal.valueOf(100000), BigDecimal.ZERO, 10, 0, Map.of(), 1.0, 1000000, true, "PAPER");
        RiskCheckResult result = riskEngine.checkOrder(request, defaultParams);
        assertFalse(result.isApproved());
        assertEquals("Max positions reached", result.reason());
    }

    @Test
    void testRiskRewardRejection() {
        OrderRiskRequest request = new OrderRiskRequest("AAPL", OrderSide.BUY, BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(90), BigDecimal.valueOf(105), BigDecimal.valueOf(100000), BigDecimal.ZERO, 0, 0, Map.of(), 1.0, 1000000, true, "PAPER");
        RiskCheckResult result = riskEngine.checkOrder(request, defaultParams);
        assertFalse(result.isApproved());
        assertEquals("Risk/Reward ratio below minimum", result.reason());
    }

    @Test
    void testValidOrderApproval() {
        OrderRiskRequest request = buildRequest();
        RiskCheckResult result = riskEngine.checkOrder(request, defaultParams);
        assertTrue(result.isApproved());
    }

    @Test
    void testPositionSizeValidation() {
        OrderRiskRequest request = new OrderRiskRequest("AAPL", OrderSide.BUY, new BigDecimal("1000"), new BigDecimal("150"), new BigDecimal("140"), new BigDecimal("170"), new BigDecimal("100000"), BigDecimal.ZERO, 0, 0, Map.of(), 2.0, 1_000_000, true, "PAPER");
        RiskCheckResult result = riskEngine.checkOrder(request, defaultParams);
        assertFalse(result.isApproved());
        assertEquals("Position size limit exceeded", result.reason());
    }

    @Test
    void testVolatilityCheck() {
        OrderRiskRequest request = new OrderRiskRequest("AAPL", OrderSide.BUY, BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(90), BigDecimal.valueOf(120), BigDecimal.valueOf(100000), BigDecimal.ZERO, 0, 0, Map.of(), 10.0, 1000000, true, "PAPER");
        RiskCheckResult result = riskEngine.checkOrder(request, defaultParams);
        assertFalse(result.isApproved());
        assertEquals("Volatility exceeds maximum", result.reason());
    }
}
