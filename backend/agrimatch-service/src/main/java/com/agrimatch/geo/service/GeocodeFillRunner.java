package com.agrimatch.geo.service;

import com.agrimatch.company.domain.BusCompany;
import com.agrimatch.company.mapper.CompanyMapper;
import com.agrimatch.geo.dto.GeoPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动时自动补全缺少坐标的公司数据（用户无感知）
 */
@Component
public class GeocodeFillRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(GeocodeFillRunner.class);

    private final CompanyMapper companyMapper;
    private final AmapGeocodeService amapGeocodeService;
    
    @Value("${amap.web-key:}")
    private String amapWebKey;

    public GeocodeFillRunner(CompanyMapper companyMapper, AmapGeocodeService amapGeocodeService) {
        this.companyMapper = companyMapper;
        this.amapGeocodeService = amapGeocodeService;
    }

    @Override
    public void run(String... args) {
        // 检查是否配置了高德 Key
        if (amapWebKey == null || amapWebKey.trim().isEmpty()) {
            log.info("[Geocode] 未配置 AMAP_WEB_KEY，跳过自动补全坐标");
            return;
        }

        // 查询缺少坐标的公司
        List<BusCompany> missing = companyMapper.selectMissingCoords();
        if (missing.isEmpty()) {
            log.info("[Geocode] 所有公司都已有坐标，无需补全");
            return;
        }

        log.info("[Geocode] 发现 {} 家公司缺少坐标，开始自动补全...", missing.size());
        
        int success = 0;
        int failed = 0;

        for (BusCompany c : missing) {
            try {
                GeoPoint p = amapGeocodeService.geocode(c.getAddress(), c.getCity(), c.getCompanyName());
                if (p != null) {
                    companyMapper.updateCoords(c.getId(), p.getLat(), p.getLng());
                    success++;
                    log.debug("[Geocode] {} -> ({}, {})", c.getCompanyName(), p.getLat(), p.getLng());
                } else {
                    failed++;
                    log.warn("[Geocode] 无法解析: {} - {}", c.getCompanyName(), c.getAddress());
                }
            } catch (Exception e) {
                failed++;
                log.warn("[Geocode] 解析异常: {} - {}", c.getCompanyName(), e.getMessage());
            }

            // 避免请求过快（高德 API 有限流）
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("[Geocode] 自动补全完成：成功 {} 家，失败 {} 家", success, failed);
    }
}

