package com.smarttrader.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smarttrader.risk.RiskParameters;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides application-level beans for the trading configuration.
 */
@Configuration
public class TradingConfig {

    /**
     * Default risk parameters bean.
     * In production, these should be loaded from application.yml or database
     * and can be tuned per strategy/session.
     * These are NEVER overridden by AI — they are set by human operators only.
     */
    @Bean
    public RiskParameters riskParameters() {
        return RiskParameters.defaults();
    }

    /**
     * Explicit ObjectMapper bean with Java 8 time module registered.
     * Uses @ConditionalOnMissingBean so Spring Boot's auto-configured one
     * takes precedence if present; otherwise this fallback is used.
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
