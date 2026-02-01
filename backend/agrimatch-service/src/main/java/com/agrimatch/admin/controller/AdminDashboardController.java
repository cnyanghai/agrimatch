package com.agrimatch.admin.controller;

import com.agrimatch.admin.AdminUtil;
import com.agrimatch.admin.dto.AdminDashboardResponse;
import com.agrimatch.admin.mapper.AdminMapper;
import com.agrimatch.common.api.Result;
import com.agrimatch.user.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    private final AdminMapper adminMapper;
    private final UserMapper userMapper;

    public AdminDashboardController(AdminMapper adminMapper, UserMapper userMapper) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public Result<AdminDashboardResponse> dashboard(Authentication authentication) {
        AdminUtil.requireAdmin(authentication, userMapper);

        AdminDashboardResponse resp = new AdminDashboardResponse();
        resp.setTotalUsers(adminMapper.countTotalUsers());
        resp.setTodayNewUsers(adminMapper.countTodayNewUsers());
        resp.setTotalCompanies(adminMapper.countTotalCompanies());
        resp.setActiveSupplyCount(adminMapper.countActiveSupplies());
        resp.setActiveRequirementCount(adminMapper.countActiveRequirements());
        resp.setTotalContracts(adminMapper.countTotalContracts());
        resp.setTotalPosts(adminMapper.countTotalPosts());
        resp.setTodayLoginCount(adminMapper.countTodayLogins());
        return Result.success(resp);
    }
}
