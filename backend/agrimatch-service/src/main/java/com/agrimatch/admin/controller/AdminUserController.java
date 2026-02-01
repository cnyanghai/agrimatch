package com.agrimatch.admin.controller;

import com.agrimatch.admin.AdminUtil;
import com.agrimatch.admin.dto.AdminUserResponse;
import com.agrimatch.admin.mapper.AdminMapper;
import com.agrimatch.common.api.PageResult;
import com.agrimatch.common.api.Result;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.user.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminMapper adminMapper;
    private final UserMapper userMapper;

    public AdminUserController(AdminMapper adminMapper, UserMapper userMapper) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public Result<PageResult<AdminUserResponse>> list(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        AdminUtil.requireAdmin(authentication, userMapper);

        int offset = (page - 1) * size;
        List<AdminUserResponse> list = adminMapper.selectUsers(keyword, offset, size);
        long total = adminMapper.countUsers(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @PutMapping("/{id}/toggle-admin")
    public Result<Void> toggleAdmin(Authentication authentication, @PathVariable("id") Long id) {
        Long adminId = AdminUtil.requireAdmin(authentication, userMapper);
        if (adminId.equals(id)) {
            throw new ApiException(400, "不能修改自己的管理员状态");
        }
        var user = userMapper.selectById(id);
        if (user == null) throw new ApiException(404, "用户不存在");
        int newFlag = (user.getIsAdmin() != null && user.getIsAdmin() == 1) ? 0 : 1;
        adminMapper.updateAdminFlag(id, newFlag);
        return Result.success();
    }

    @PutMapping("/{id}/toggle-status")
    public Result<Void> toggleStatus(Authentication authentication, @PathVariable("id") Long id) {
        Long adminId = AdminUtil.requireAdmin(authentication, userMapper);
        if (adminId.equals(id)) {
            throw new ApiException(400, "不能禁用自己");
        }
        var user = userMapper.selectById(id);
        if (user == null) throw new ApiException(404, "用户不存在");
        int newDeleted = (user.getIsDeleted() != null && user.getIsDeleted() == 1) ? 0 : 1;
        adminMapper.updateUserDeleted(id, newDeleted);
        return Result.success();
    }
}
