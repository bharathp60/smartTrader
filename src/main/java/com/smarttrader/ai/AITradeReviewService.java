package com.smarttrader.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AITradeReviewService {

    private final ChatClient chatClient;

    public AITradeReviewService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String reviewDayTrades(List<String> trades, String marketSummary) {
        String prompt = String.format("""
            Review the following trades executed today in the context of the overall market summary.
            Market Summary: %s
            Trades: %s
            
            Provide structured feedback highlighting key takeaways, strengths, and areas for improvement. Format as JSON.
            """, marketSummary, trades.toString());
            
        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            return "{\"error\": \"AI review unavailable.\"}";
        }
    }
}
