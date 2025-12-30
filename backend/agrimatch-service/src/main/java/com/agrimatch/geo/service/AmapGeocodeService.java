package com.agrimatch.geo.service;

import com.agrimatch.geo.dto.GeoPoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AmapGeocodeService {
    private static final Logger log = LoggerFactory.getLogger(AmapGeocodeService.class);
    private final String webKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final Pattern CITY_PATTERN = Pattern.compile("([\\p{IsHan}]{2,12}市)");

    public AmapGeocodeService(@Value("${amap.web-key:}") String webKey, ObjectMapper objectMapper) {
        this.webKey = webKey;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    /**
     * 地址 -> 坐标（高德地理编码）
     * 返回 null 表示未配置 key 或无法解析。
     */
    @SuppressWarnings("null") // StandardCharsets.UTF_8 被静态分析误报为可能为 null
    public GeoPoint geocode(String address, String city) {
        return geocode(address, city, null);
    }

    /**
     * 地址 -> 坐标（优先地理编码；失败后用 POI 关键字检索兜底）
     * 返回 null 表示未配置 key 或无法解析。
     */
    @SuppressWarnings("null") // StandardCharsets.UTF_8 被静态分析误报为可能为 null
    public GeoPoint geocode(String address, String city, String keywordFallback) {
        if (!StringUtils.hasText(webKey)) return null;
        if (!StringUtils.hasText(address) && !StringUtils.hasText(keywordFallback)) return null;

        // 优先使用明确 city；否则从地址/公司名里尽量提取 “xx市” 作为 citylimit，提高命中率
        String inferredCity = StringUtils.hasText(city) ? city : inferCity(address, keywordFallback);

        // 1) 地理编码：地址 -> 坐标
        if (StringUtils.hasText(address)) {
            GeoPoint p = tryGeocode(address, inferredCity);
            if (p != null) return p;
        }

        // 2) POI 检索兜底：用“地址/公司名”当关键词搜 POI
        if (StringUtils.hasText(address)) {
            GeoPoint p = tryPlaceText(address, inferredCity);
            if (p != null) return p;
        }
        if (StringUtils.hasText(keywordFallback) && !keywordFallback.trim().equals(address != null ? address.trim() : "")) {
            GeoPoint p = tryPlaceText(keywordFallback, inferredCity);
            if (p != null) return p;
        }
        return null;
    }

    @SuppressWarnings("null") // StandardCharsets.UTF_8 静态分析误报
    private GeoPoint tryGeocode(String address, String city) {
        // 重要：地址可能含中文，必须做 URL 编码，否则会触发 IllegalArgumentException: Invalid character for QUERY_PARAM
        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://restapi.amap.com/v3/geocode/geo")
                .queryParam("key", webKey)
                .queryParam("address", address)
                .queryParam("city", StringUtils.hasText(city) ? city : null)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        try {
            String body = restTemplate.getForObject(uri, String.class);
            if (!StringUtils.hasText(body)) return null;
            JsonNode root = objectMapper.readTree(body);
            if (!"1".equals(root.path("status").asText())) {
                log.warn("AMap geocode failed: status={}, info={}, infocode={}, address={}, city={}",
                        root.path("status").asText(),
                        root.path("info").asText(),
                        root.path("infocode").asText(),
                        address,
                        city);
                return null;
            }
            JsonNode geocodes = root.path("geocodes");
            if (!geocodes.isArray() || geocodes.size() == 0) return null;
            String location = geocodes.get(0).path("location").asText();
            return parseLocation(location);
        } catch (Exception ignore) {
            return null;
        }
    }

    @SuppressWarnings("null") // StandardCharsets.UTF_8 静态分析误报
    private GeoPoint tryPlaceText(String keywords, String city) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://restapi.amap.com/v3/place/text")
                .queryParam("key", webKey)
                .queryParam("keywords", keywords)
                .queryParam("city", StringUtils.hasText(city) ? city : null)
                .queryParam("citylimit", StringUtils.hasText(city) ? "true" : null)
                .queryParam("offset", 1)
                .queryParam("page", 1)
                .queryParam("extensions", "base")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();
        try {
            String body = restTemplate.getForObject(uri, String.class);
            if (!StringUtils.hasText(body)) return null;
            JsonNode root = objectMapper.readTree(body);
            if (!"1".equals(root.path("status").asText())) {
                log.warn("AMap place/text failed: status={}, info={}, infocode={}, keywords={}, city={}",
                        root.path("status").asText(),
                        root.path("info").asText(),
                        root.path("infocode").asText(),
                        keywords,
                        city);
                return null;
            }
            JsonNode pois = root.path("pois");
            if (!pois.isArray() || pois.size() == 0) return null;
            String location = pois.get(0).path("location").asText();
            return parseLocation(location);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String inferCity(String address, String keywordFallback) {
        String s = StringUtils.hasText(address) ? address : keywordFallback;
        if (!StringUtils.hasText(s)) return null;
        Matcher m = CITY_PATTERN.matcher(s);
        if (m.find()) return m.group(1);
        return null;
    }

    private static GeoPoint parseLocation(String location) {
        if (!StringUtils.hasText(location) || !location.contains(",")) return null;
        String[] parts = location.split(",");
        if (parts.length != 2) return null;
        BigDecimal lng = new BigDecimal(parts[0]);
        BigDecimal lat = new BigDecimal(parts[1]);
        return new GeoPoint(lat, lng);
    }
}





