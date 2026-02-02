package com.agrimatch.controller;

import com.agrimatch.common.api.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @Value("${app.filing-mode:false}")
    private boolean filingMode;

    @GetMapping("/")
    public Result<String> home() {
        return Result.success("AgriMatch 后端服务运行中。请访问 /api/* 接口或前端演示页。");
    }

    @GetMapping("/api/health")
    public Result<String> health() {
        return Result.success("OK");
    }

    @GetMapping("/api/config")
    public Result<Map<String, Object>> config() {
        return Result.success(Map.of("filingMode", filingMode));
    }
}


