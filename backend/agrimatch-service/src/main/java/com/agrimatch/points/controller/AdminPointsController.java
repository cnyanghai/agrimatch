package com.agrimatch.points.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.points.dto.AdminFulfillRequest;
import com.agrimatch.points.dto.AdminJdRedeemResponse;
import com.agrimatch.points.service.PointsService;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/jd-redeems")
@Validated
public class AdminPointsController {
    private final PointsService pointsService;
    private final UserMapper userMapper;

    public AdminPointsController(PointsService pointsService, UserMapper userMapper) {
        this.pointsService = pointsService;
        this.userMapper = userMapper;
    }

    private Long requireAdmin(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getIsAdmin() == null || user.getIsAdmin() != 1) {
            throw new ApiException(403, "无管理员权限");
        }
        return userId;
    }

    @GetMapping
    public Result<List<AdminJdRedeemResponse>> list(
            Authentication authentication,
            @RequestParam(required = false) Integer status) {
        requireAdmin(authentication);
        return Result.success(pointsService.listAllJdRedeems(status));
    }

    @PostMapping("/{id}/fulfill")
    public Result<Void> fulfill(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AdminFulfillRequest req) {
        Long adminUserId = requireAdmin(authentication);
        pointsService.fulfillJdRedeem(adminUserId, id, req.getCardCode());
        return Result.success();
    }

    @PostMapping("/{id}/fail")
    public Result<Void> fail(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        Long adminUserId = requireAdmin(authentication);
        String remark = body != null ? body.get("remark") : null;
        pointsService.failJdRedeem(adminUserId, id, remark);
        return Result.success();
    }
}
