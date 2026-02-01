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
@RequestMapping("/api/admin/posts")
public class AdminPostController {
    private final AdminMapper adminMapper;
    private final UserMapper userMapper;

    public AdminPostController(AdminMapper adminMapper, UserMapper userMapper) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        AdminUtil.requireAdmin(authentication, userMapper);
        int offset = (page - 1) * size;
        List<Map<String, Object>> list = adminMapper.selectPosts(keyword, offset, size);
        long total = adminMapper.countPosts(keyword);
        return Result.success(new PageResult<>(list, total, page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication authentication, @PathVariable("id") Long id) {
        AdminUtil.requireAdmin(authentication, userMapper);
        adminMapper.softDeletePost(id);
        return Result.success();
    }
}
