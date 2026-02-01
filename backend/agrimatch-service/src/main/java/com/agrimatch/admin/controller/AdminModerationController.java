package com.agrimatch.admin.controller;

import com.agrimatch.admin.AdminUtil;
import com.agrimatch.admin.mapper.AdminMapper;
import com.agrimatch.common.api.PageResult;
import com.agrimatch.common.api.Result;
import com.agrimatch.user.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminModerationController {
    private final AdminMapper adminMapper;
    private final UserMapper userMapper;

    public AdminModerationController(AdminMapper adminMapper, UserMapper userMapper) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
    }

    // ==================== 供应审核 ====================

    @GetMapping("/supplies")
    public Result<PageResult<Map<String, Object>>> listSupplies(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        AdminUtil.requireAdmin(authentication, userMapper);
        int offset = (page - 1) * size;
        List<Map<String, Object>> list = adminMapper.selectSupplies(keyword, offset, size);
        long total = adminMapper.countSupplies(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @PutMapping("/supplies/{id}/takedown")
    public Result<Void> takedownSupply(Authentication authentication, @PathVariable("id") Long id) {
        AdminUtil.requireAdmin(authentication, userMapper);
        adminMapper.updateSupplyStatus(id, 2);
        return Result.success();
    }

    @PutMapping("/supplies/{id}/restore")
    public Result<Void> restoreSupply(Authentication authentication, @PathVariable("id") Long id) {
        AdminUtil.requireAdmin(authentication, userMapper);
        adminMapper.updateSupplyStatus(id, 0);
        return Result.success();
    }

    // ==================== 采购审核 ====================

    @GetMapping("/requirements")
    public Result<PageResult<Map<String, Object>>> listRequirements(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        AdminUtil.requireAdmin(authentication, userMapper);
        int offset = (page - 1) * size;
        List<Map<String, Object>> list = adminMapper.selectRequirements(keyword, offset, size);
        long total = adminMapper.countRequirements(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @PutMapping("/requirements/{id}/takedown")
    public Result<Void> takedownRequirement(Authentication authentication, @PathVariable("id") Long id) {
        AdminUtil.requireAdmin(authentication, userMapper);
        adminMapper.updateRequirementStatus(id, 2);
        return Result.success();
    }

    @PutMapping("/requirements/{id}/restore")
    public Result<Void> restoreRequirement(Authentication authentication, @PathVariable("id") Long id) {
        AdminUtil.requireAdmin(authentication, userMapper);
        adminMapper.updateRequirementStatus(id, 0);
        return Result.success();
    }
}
