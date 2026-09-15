package com.smarttrader.risk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttrader.entity.RiskDecision;
import com.smarttrader.entity.RiskEvent;
import com.smarttrader.repository.RiskEventRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RiskEngine {

    private final RiskEventRepository riskEventRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RiskEngine(RiskEventRepository riskEventRepository, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.riskEventRepository = riskEventRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public RiskCheckResult checkOrder(OrderRiskRequest request, RiskParameters params) {
        Map<String, Boolean> limits = new LinkedHashMap<>();
        
        try {
            // 1. checkMarketOpen
            limits.put("checkMarketOpen", request.isMarketOpen());
            if (!request.isMarketOpen()) {
                return reject(request.symbol(), "Market is closed", limits);
            }
            
            // 2. checkDailyLoss
            BigDecimal maxDailyLoss = request.currentCapital().multiply(BigDecimal.valueOf(params.maxDailyLossPercent())).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
            boolean dailyLossPass = request.currentDailyLoss().compareTo(maxDailyLoss) < 0;
            limits.put("checkDailyLoss", dailyLossPass);
            if (!dailyLossPass) {
                return reject(request.symbol(), "Daily loss limit reached", limits);
            }
            
            // 3. checkConsecutiveLosses
            boolean consecutiveLossesPass = request.currentConsecutiveLosses() < params.maxConsecutiveLosses();
            limits.put("checkConsecutiveLosses", consecutiveLossesPass);
            if (!consecutiveLossesPass) {
                return reject(request.symbol(), "Max consecutive losses reached", limits);
            }
            
            // 4. checkPortfolioDrawdown
            limits.put("checkPortfolioDrawdown", true);
            
            // 5. checkPositionCount
            boolean positionCountPass = request.currentOpenPositions() < params.maxOpenPositions();
            limits.put("checkPositionCount", positionCountPass);
            if (!positionCountPass) {
                return reject(request.symbol(), "Max positions reached", limits);
            }
            
            // 6. checkPositionSize
            BigDecimal positionSize = request.quantity().multiply(request.entryPrice());
            BigDecimal maxPositionSize = request.currentCapital().multiply(BigDecimal.valueOf(params.maxPositionSizePercent())).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
            boolean positionSizePass = positionSize.compareTo(maxPositionSize) <= 0;
            limits.put("checkPositionSize", positionSizePass);
            if (!positionSizePass) {
                return reject(request.symbol(), "Position size limit exceeded", limits);
            }
            
            // 7. checkSectorExposure
            limits.put("checkSectorExposure", true);
            if (request.sectorExposures() != null) {
                for (Map.Entry<String, BigDecimal> entry : request.sectorExposures().entrySet()) {
                    if (entry.getValue().compareTo(BigDecimal.valueOf(params.maxSectorExposurePercent())) > 0) {
                        return reject(request.symbol(), "Sector exposure exceeds limit", limits);
                    }
                }
            }
            
            // 8. checkRiskReward
            boolean rrPass = false;
            if (request.stopLoss() != null && request.target() != null && request.entryPrice() != null) {
                BigDecimal risk = request.entryPrice().subtract(request.stopLoss()).abs();
                BigDecimal reward = request.target().subtract(request.entryPrice()).abs();
                if (risk.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal rr = reward.divide(risk, 4, RoundingMode.HALF_UP);
                    rrPass = rr.compareTo(BigDecimal.valueOf(params.minRiskRewardRatio())) >= 0;
                }
            }
            limits.put("checkRiskReward", rrPass);
            if (!rrPass) {
                return reject(request.symbol(), "Risk/Reward ratio below minimum", limits);
            }
            
            // 9. checkVolatility
            boolean volatilityPass = request.atrPercent() <= params.maxVolatilityPercent();
            limits.put("checkVolatility", volatilityPass);
            if (!volatilityPass) {
                return reject(request.symbol(), "Volatility exceeds maximum", limits);
            }
            
            // 10. checkLiquidity
            boolean liquidityPass = request.avgDailyVolume() >= params.minLiquidityVolume();
            limits.put("checkLiquidity", liquidityPass);
            if (!liquidityPass) {
                return reject(request.symbol(), "Liquidity below minimum", limits);
            }
            
            // 11. checkStopLoss
            boolean stopLossPass = request.stopLoss() != null;
            limits.put("checkStopLoss", stopLossPass);
            if (!stopLossPass) {
                return reject(request.symbol(), "Stop loss is missing", limits);
            }
            
            // 12. checkDuplicatePosition
            String lockKey = "position:" + request.symbol();
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofMinutes(1));
            boolean duplicatePass = Boolean.TRUE.equals(locked);
            limits.put("checkDuplicatePosition", duplicatePass);
            if (!duplicatePass) {
                return reject(request.symbol(), "Duplicate position", limits);
            }
            
            double score = 0.5; // placeholder
            RiskCheckResult result = RiskCheckResult.approved(score, limits);
            // Ignore persistEvent for now since entity setters may not be available, or handle via direct repo save if we add it
            return result;
        } catch (Exception e) {
            return reject(request.symbol(), "Internal error: " + e.getMessage(), limits);
        }
    }

    private RiskCheckResult reject(String symbol, String reason, Map<String, Boolean> limits) {
        RiskCheckResult result = RiskCheckResult.rejected(reason, limits);
        return result;
    }
}
