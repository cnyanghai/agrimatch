package com.agrimatch.home.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.home.dto.DashboardResponse;
import com.agrimatch.home.service.DashboardService;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UserMapper userMapper;
    
    public DashboardController(DashboardService dashboardService, UserMapper userMapper) {
        this.dashboardService = dashboardService;
        this.userMapper = userMapper;
    }
    
    /**
     * 获取控制台首页数据
     * 包含待办事项统计和业务数据统计
     */
    @GetMapping
    public Result<DashboardResponse> getDashboard(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "用户未登录");
        }
        
        // 获取用户角色信息
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        
        boolean isBuyer = user.getIsBuyer() != null && user.getIsBuyer() == 1;
        boolean isSeller = user.getIsSeller() != null && user.getIsSeller() == 1;
        
        DashboardResponse data = dashboardService.getDashboardData(userId, isBuyer, isSeller);
        return Result.success(data);
    }
    
    /**
     * 获取待办事项总数（用于显示角标/徽章）
     */
    @GetMapping("/pending-count")
    public Result<Integer> getPendingCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "用户未登录");
        }
        
        int count = dashboardService.getTotalPendingCount(userId);
        return Result.success(count);
    }
}

