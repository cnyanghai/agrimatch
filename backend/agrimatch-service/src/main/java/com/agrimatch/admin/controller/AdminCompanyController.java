package com.agrimatch.admin.controller;

import com.agrimatch.admin.AdminUtil;
import com.agrimatch.admin.dto.AdminCompanyResponse;
import com.agrimatch.admin.mapper.AdminMapper;
import com.agrimatch.common.api.PageResult;
import com.agrimatch.common.api.Result;
import com.agrimatch.user.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/companies")
public class AdminCompanyController {
    private final AdminMapper adminMapper;
    private final UserMapper userMapper;

    public AdminCompanyController(AdminMapper adminMapper, UserMapper userMapper) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public Result<PageResult<AdminCompanyResponse>> list(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        AdminUtil.requireAdmin(authentication, userMapper);
        if (size > 100) size = 100;

        int offset = (page - 1) * size;
        List<AdminCompanyResponse> list = adminMapper.selectCompanies(keyword, status, offset, size);
        long total = adminMapper.countCompanies(keyword, status);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @PutMapping("/{id}/verify")
    public Result<Void> verify(Authentication authentication, @PathVariable("id") Long id) {
        AdminUtil.requireAdmin(authentication, userMapper);
        adminMapper.updateCompanyVerifiedStatus(id, 1);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    public Result<Void> reject(Authentication authentication, @PathVariable("id") Long id) {
        AdminUtil.requireAdmin(authentication, userMapper);
        adminMapper.updateCompanyVerifiedStatus(id, 2);
        return Result.success();
    }
}
