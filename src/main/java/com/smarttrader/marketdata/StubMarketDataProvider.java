package com.smarttrader.marketdata;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Component
@Profile("!live")
public class StubMarketDataProvider implements MarketDataProvider {

    private static final String PROVIDER_NAME = "STUB";
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private final Random random = new Random();

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public List<CandleDto> getHistoricalCandles(String symbol, Timeframe timeframe, Instant from, Instant to) {
        List<CandleDto> candles = new ArrayList<>();
        BigDecimal basePrice = getBasePrice(symbol);
        
        long stepSeconds = getStepSeconds(timeframe);
        Instant current = from;
        
        BigDecimal lastClose = basePrice;
        
        while (!current.isAfter(to)) {
            ZonedDateTime zdt = current.atZone(IST_ZONE);
            boolean isWeekend = zdt.getDayOfWeek() == DayOfWeek.SATURDAY || zdt.getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean isMarketHours = isMarketHours(zdt);
            
            if (!isWeekend && isMarketHours) {
                double changePct = (random.nextDouble() - 0.5) * 0.005; // +/- 0.25%
                BigDecimal open = lastClose;
                BigDecimal close = open.multiply(BigDecimal.valueOf(1 + changePct)).setScale(2, RoundingMode.HALF_UP);
                
                BigDecimal high = open.max(close).multiply(BigDecimal.valueOf(1 + random.nextDouble() * 0.002)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal low = open.min(close).multiply(BigDecimal.valueOf(1 - random.nextDouble() * 0.002)).setScale(2, RoundingMode.HALF_UP);
                
                BigDecimal volume = BigDecimal.valueOf(1000 + random.nextInt(9000));
                
                candles.add(new CandleDto(symbol, timeframe.getCode(), current, open, high, low, close, volume, PROVIDER_NAME));
                lastClose = close;
            }
            
            current = current.plus(stepSeconds, ChronoUnit.SECONDS);
        }
        
        return candles;
    }

    @Override
    public Optional<QuoteDto> getLatestQuote(String symbol) {
        BigDecimal basePrice = getBasePrice(symbol);
        double noise = (random.nextDouble() - 0.5) * 0.01; // +/- 0.5%
        BigDecimal lastPrice = basePrice.multiply(BigDecimal.valueOf(1 + noise)).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal bidPrice = lastPrice.multiply(BigDecimal.valueOf(0.999)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal askPrice = lastPrice.multiply(BigDecimal.valueOf(1.001)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal volume = BigDecimal.valueOf(random.nextInt(10000));
        
        return Optional.of(new QuoteDto(symbol, lastPrice, bidPrice, askPrice, volume, Instant.now(), PROVIDER_NAME));
    }

    @Override
    public boolean isMarketOpen() {
        ZonedDateTime now = ZonedDateTime.now(IST_ZONE);
        DayOfWeek day = now.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }
        return isMarketHours(now);
    }

    @Override
    public Set<String> getSupportedSymbols() {
        return Set.of("RELIANCE", "TCS", "HDFCBANK", "INFY");
    }

    private BigDecimal getBasePrice(String symbol) {
        return switch (symbol.toUpperCase()) {
            case "RELIANCE" -> new BigDecimal("2800");
            case "TCS" -> new BigDecimal("3500");
            default -> new BigDecimal("1000");
        };
    }
    
    private boolean isMarketHours(ZonedDateTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        int timeInMinutes = hour * 60 + minute;
        
        int startMinutes = 9 * 60 + 15; // 9:15
        int endMinutes = 15 * 60 + 30; // 15:30
        
        return timeInMinutes >= startMinutes && timeInMinutes <= endMinutes;
    }
    
    private long getStepSeconds(Timeframe timeframe) {
        return switch (timeframe) {
            case ONE_MINUTE -> 60;
            case FIVE_MINUTES -> 300;
            case FIFTEEN_MINUTES -> 900;
            case THIRTY_MINUTES -> 1800;
            case ONE_HOUR -> 3600;
            case ONE_DAY -> 86400;
        };
    }
}
