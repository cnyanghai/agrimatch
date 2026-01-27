package com.agrimatch.home.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.home.dto.DashboardResponse;
import com.agrimatch.home.service.DashboardService;
import com.agrimatch.util.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制台首页API
 * 提供待办事项和业务统计数据
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取控制台首页数据
     * 包含待办事项统计和业务数据统计
     * 注意：系统不再区分供应商/采购商，统计用户的所有发布
     */
    @GetMapping
    public Result<DashboardResponse> getDashboard(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        DashboardResponse data = dashboardService.getDashboardData(userId);
        return Result.success(data);
    }

    /**
     * 获取待办事项总数（用于显示角标/徽章）
     */
    @GetMapping("/pending-count")
    public Result<Integer> getPendingCount(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        int count = dashboardService.getTotalPendingCount(userId);
        return Result.success(count);
    }
}

