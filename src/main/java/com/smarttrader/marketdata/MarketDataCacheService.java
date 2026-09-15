package com.smarttrader.marketdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class MarketDataCacheService {

    private static final String QUOTE_KEY_PREFIX = "quote:";
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public MarketDataCacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void cacheQuote(QuoteDto quote, Duration ttl) {
        try {
            String key = QUOTE_KEY_PREFIX + quote.symbol();
            String json = objectMapper.writeValueAsString(quote);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize QuoteDto", e);
        }
    }

    public Optional<QuoteDto> getCachedQuote(String symbol) {
        String key = QUOTE_KEY_PREFIX + symbol;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, QuoteDto.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public void evictQuote(String symbol) {
        String key = QUOTE_KEY_PREFIX + symbol;
        redisTemplate.delete(key);
    }
}
