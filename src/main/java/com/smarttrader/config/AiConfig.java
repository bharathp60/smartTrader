package com.smarttrader.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem("You are a quantitative trading analyst for the SMART TRADER platform. " +
                "You analyze stocks based on technical indicators, ML predictions, and market data. " +
                "You are conservative and prefer capital preservation over aggressive trading. " +
                "Always return structured JSON responses. Never invent or hallucinate market data.")
            .build();
    }
}
