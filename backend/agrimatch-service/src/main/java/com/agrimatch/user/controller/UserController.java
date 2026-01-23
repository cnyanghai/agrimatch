package com.agrimatch.user.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.user.dto.UserCreateRequest;
import com.agrimatch.user.dto.UserBriefResponse;
import com.agrimatch.user.dto.UserResponse;
import com.agrimatch.user.dto.UserRoleUpdateRequest;
import com.agrimatch.user.dto.UserUpdateRequest;
import com.agrimatch.user.domain.SysLoginLog;
import com.agrimatch.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateRequest req) {
        return Result.success(userService.create(req));
    }

    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable("id") @NotNull Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/me")
    public Result<UserResponse> me(Authentication authentication) {
        Long userId = com.agrimatch.util.SecurityUtil.requireUserId(authentication);
        return Result.success(userService.getById(userId));
    }

    @GetMapping("/login-logs")
    public Result<List<SysLoginLog>> getLoginLogs(Authentication authentication) {
        String userName = ((com.agrimatch.security.LoginUser) authentication.getPrincipal()).getUserName();
        return Result.success(userService.getLoginLogs(userName, 10));
    }

    @GetMapping("/search")
    public Result<List<UserBriefResponse>> search(Authentication authentication,
                                                 @RequestParam("keyword") String keyword,
                                                 @RequestParam(value = "limit", required = false) Integer limit) {
        // require login
        com.agrimatch.util.SecurityUtil.requireUserId(authentication);
        return Result.success(userService.search(keyword, limit));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") @NotNull Long id,
                               @Valid @RequestBody UserUpdateRequest req) {
        userService.update(id, req);
        return Result.success();
    }

    @PutMapping("/me")
    public Result<Void> updateMe(Authentication authentication, @Valid @RequestBody UserUpdateRequest req) {
        Long userId = com.agrimatch.util.SecurityUtil.requireUserId(authentication);
        userService.update(userId, req);
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    public Result<Void> updateRoles(@PathVariable("id") @NotNull Long id,
                                    @Valid @RequestBody UserRoleUpdateRequest req) {
        userService.updateRoles(id, req);
        return Result.success();
    }

    @PutMapping("/me/roles")
    public Result<Void> updateMyRoles(Authentication authentication, @Valid @RequestBody UserRoleUpdateRequest req) {
        Long userId = com.agrimatch.util.SecurityUtil.requireUserId(authentication);
        userService.updateRoles(userId, req);
        return Result.success();
    }
}


