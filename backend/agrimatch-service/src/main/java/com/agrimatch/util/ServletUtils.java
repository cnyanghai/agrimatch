package com.agrimatch.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class ServletUtils {
    
    public static Optional<HttpServletRequest> getRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
    }

    public static String getClientIp() {
        return getRequest().map(request -> {
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
        }).orElse("unknown");
    }

    public static String getUserAgent() {
        return getRequest().map(request -> request.getHeader("User-Agent")).orElse("");
    }

    public static String getBrowser(String ua) {
        if (ua == null || ua.isEmpty()) return "Unknown";
        if (ua.contains("Edg/")) return "Edge";
        if (ua.contains("Chrome/")) return "Chrome";
        if (ua.contains("Firefox/")) return "Firefox";
        if (ua.contains("Safari/") && !ua.contains("Chrome/")) return "Safari";
        return "Other";
    }

    public static String getOs(String ua) {
        if (ua == null || ua.isEmpty()) return "Unknown";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac")) return "Mac OS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return "Other";
    }
}

