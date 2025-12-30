package com.agrimatch.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 简单地理工具：用于 Demo/MVP 的距离估算（不依赖高德额度）。
 */
public class GeoUtil {
    private GeoUtil() {
    }

    /**
     * Haversine 大圆距离（公里）。
     * @return km，保留 3 位小数；若任一坐标为空则返回 null
     */
    public static BigDecimal haversineKm(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) return null;
        double r = 6371.0088d; // Earth radius in km
        double la1 = Math.toRadians(lat1.doubleValue());
        double lo1 = Math.toRadians(lng1.doubleValue());
        double la2 = Math.toRadians(lat2.doubleValue());
        double lo2 = Math.toRadians(lng2.doubleValue());
        double dLat = la2 - la1;
        double dLon = lo2 - lo1;
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(la1) * Math.cos(la2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.asin(Math.min(1, Math.sqrt(a)));
        double km = r * c;
        return BigDecimal.valueOf(km).setScale(3, RoundingMode.HALF_UP);
    }
}


