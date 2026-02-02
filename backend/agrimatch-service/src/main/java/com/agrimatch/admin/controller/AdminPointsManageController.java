package com.agrimatch.admin.controller;

import com.agrimatch.admin.AdminUtil;
import com.agrimatch.admin.dto.AdminPointsOverviewResponse;
import com.agrimatch.admin.mapper.AdminMapper;
import com.agrimatch.common.api.PageResult;
import com.agrimatch.common.api.Result;
import com.agrimatch.points.dto.GiftResponse;
import com.agrimatch.user.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/points-manage")
public class AdminPointsManageController {
    private final AdminMapper adminMapper;
    private final UserMapper userMapper;

    public AdminPointsManageController(AdminMapper adminMapper, UserMapper userMapper) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/overview")
    public Result<AdminPointsOverviewResponse> overview(Authentication authentication) {
        AdminUtil.requireAdmin(authentication, userMapper);
        AdminPointsOverviewResponse resp = new AdminPointsOverviewResponse();
        resp.setTotalRechargeAmount(adminMapper.sumTotalRechargeAmount());
        resp.setTodayRechargeAmount(adminMapper.sumTodayRechargeAmount());
        resp.setTotalCardAmount(adminMapper.sumTotalCardAmount());
        resp.setTodayCardAmount(adminMapper.sumTodayCardAmount());
        resp.setTotalGiftPoints(adminMapper.sumTotalGiftPoints());
        resp.setTotalCirculatingPoints(adminMapper.sumTotalCirculatingPoints());
        return Result.success(resp);
    }

    @GetMapping("/recharges")
    public Result<PageResult<Map<String, Object>>> recharges(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        AdminUtil.requireAdmin(authentication, userMapper);
        if (size > 100) size = 100;
        int offset = (page - 1) * size;
        List<Map<String, Object>> list = adminMapper.selectRechargeRecords(keyword, offset, size);
        long total = adminMapper.countRechargeRecords(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @GetMapping("/recharge-users")
    public Result<PageResult<Map<String, Object>>> rechargeUsers(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        AdminUtil.requireAdmin(authentication, userMapper);
        if (size > 100) size = 100;
        int offset = (page - 1) * size;
        List<Map<String, Object>> list = adminMapper.selectRechargeUsers(keyword, offset, size);
        long total = adminMapper.countRechargeUsers(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @GetMapping("/gifts")
    public Result<PageResult<GiftResponse>> gifts(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        AdminUtil.requireAdmin(authentication, userMapper);
        if (size > 100) size = 100;
        int offset = (page - 1) * size;
        List<GiftResponse> list = adminMapper.selectGiftRecords(keyword, offset, size);
        long total = adminMapper.countGiftRecords(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }
}
