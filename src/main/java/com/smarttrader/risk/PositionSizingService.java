package com.smarttrader.risk;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PositionSizingService {

    public BigDecimal calculateSize(BigDecimal capital, BigDecimal entryPrice, BigDecimal stopLoss, double riskPercent, BigDecimal lotSize, double maxPositionValuePct) {
        if (entryPrice == null || stopLoss == null || entryPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal riskAmount = capital.multiply(BigDecimal.valueOf(riskPercent)).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        BigDecimal stopDistance = entryPrice.subtract(stopLoss).abs();

        if (stopDistance.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal rawQuantity = riskAmount.divide(stopDistance, 4, RoundingMode.HALF_UP);
        BigDecimal maxPositionValue = capital.multiply(BigDecimal.valueOf(maxPositionValuePct)).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        BigDecimal maxQuantityByValue = maxPositionValue.divide(entryPrice, 4, RoundingMode.HALF_UP);

        BigDecimal quantity = rawQuantity.min(maxQuantityByValue);
        
        if (lotSize != null && lotSize.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal lots = quantity.divide(lotSize, 0, RoundingMode.DOWN);
            quantity = lots.multiply(lotSize);
        } else {
            quantity = quantity.setScale(0, RoundingMode.DOWN);
        }

        return quantity;
    }

    public boolean validateSize(BigDecimal quantity, BigDecimal entryPrice, BigDecimal capital, double maxPositionValuePct) {
        if (quantity == null || entryPrice == null || capital == null) {
            return false;
        }
        BigDecimal positionValue = quantity.multiply(entryPrice);
        BigDecimal maxPositionValue = capital.multiply(BigDecimal.valueOf(maxPositionValuePct)).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        return positionValue.compareTo(maxPositionValue) <= 0;
    }
}
