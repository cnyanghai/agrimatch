package com.agrimatch.controller;

import com.agrimatch.common.api.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public Result<String> home() {
        return Result.success("AgriMatch 后端服务运行中。请访问 /api/* 接口或前端演示页。");
    }

    @GetMapping("/api/health")
    public Result<String> health() {
        return Result.success("OK");
    }
}


