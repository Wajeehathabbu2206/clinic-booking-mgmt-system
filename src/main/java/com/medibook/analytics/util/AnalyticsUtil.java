package com.medibook.analytics.util;

import java.math.BigDecimal;

public final class AnalyticsUtil {

    private AnalyticsUtil() {
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(value.toString());
    }

    public static double toDouble(Object value) {
        return value == null ? 0.0 : ((Number) value).doubleValue();
    }

    public static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }
}