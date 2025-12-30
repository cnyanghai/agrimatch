package com.agrimatch.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 单号生成工具（MVP）：不追求严格序列，只要“基本唯一、可读”。
 */
public class NoUtil {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private NoUtil() {
    }

    /**
     * 生成单号：PREFIX + yyyyMMdd + 6位时间戳尾数
     */
    public static String gen(String prefix) {
        String day = LocalDate.now().format(DAY);
        long tail = System.currentTimeMillis() % 1_000_000L;
        return prefix + day + String.format("%06d", tail);
    }
}


