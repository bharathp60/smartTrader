package com.smarttrader.screener;

import java.util.Map;

public record ScreeningResult(
    String symbol,
    boolean passedLiquidity,
    boolean passedVolume,
    boolean passedTechnical,
    boolean passedMl,
    Map<String, Object> details
) {
    public boolean isCandidate() { 
        return passedLiquidity && passedVolume && passedTechnical; 
    }
}
