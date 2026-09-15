package com.smarttrader.indicators;

import java.util.Map;

public record IndicatorResult(String name, double value, Map<String, Double> components) {}
