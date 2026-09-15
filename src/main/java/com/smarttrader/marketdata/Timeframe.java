package com.smarttrader.marketdata;

public enum Timeframe {
    ONE_MINUTE("1m"), FIVE_MINUTES("5m"), FIFTEEN_MINUTES("15m"),
    THIRTY_MINUTES("30m"), ONE_HOUR("1h"), ONE_DAY("1d");

    private final String code;

    Timeframe(String code) { this.code = code; }

    public String getCode() { return code; }

    public static Timeframe fromCode(String code) {
        for (Timeframe tf : values()) {
            if (tf.code.equals(code)) return tf;
        }
        throw new IllegalArgumentException("Unknown timeframe: " + code);
    }
}
