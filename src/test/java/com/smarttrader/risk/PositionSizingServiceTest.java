package com.smarttrader.risk;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PositionSizingServiceTest {

    private final PositionSizingService service = new PositionSizingService();

    @Test
    void testBasicPositionSizeCalculation() {
        BigDecimal size = service.calculateSize(new BigDecimal("100000"), new BigDecimal("100"), new BigDecimal("90"), 1.0, null, 10.0);
        // Risk = 1000, Stop distance = 10 -> Quantity = 100
        assertEquals(new BigDecimal("100"), size);
    }

    @Test
    void testLotSizeRounding() {
        BigDecimal size = service.calculateSize(new BigDecimal("100000"), new BigDecimal("100"), new BigDecimal("90"), 1.0, new BigDecimal("50"), 10.0);
        assertEquals(new BigDecimal("100"), size);
    }

    @Test
    void testMaxPositionValueConstraint() {
        BigDecimal size = service.calculateSize(new BigDecimal("100000"), new BigDecimal("100"), new BigDecimal("99"), 2.0, null, 10.0);
        // Risk = 2000, Stop dist = 1 -> Raw Qty = 2000. Pos Val = 200,000. Max Pos Val = 10% of 100k = 10,000.
        // Qty = 10,000 / 100 = 100
        assertEquals(new BigDecimal("100"), size);
    }

    @Test
    void testZeroStopDistance() {
        BigDecimal size = service.calculateSize(new BigDecimal("100000"), new BigDecimal("100"), new BigDecimal("100"), 1.0, null, 10.0);
        assertEquals(BigDecimal.ZERO, size);
    }

    @Test
    void testStandard1PercentRisk() {
        BigDecimal size = service.calculateSize(new BigDecimal("10000"), new BigDecimal("50"), new BigDecimal("45"), 1.0, null, 10.0);
        // Risk = 100, Stop dist = 5 -> Qty = 20
        assertEquals(new BigDecimal("20"), size);
    }
}
