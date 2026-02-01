package com.agrimatch.points.controller;

import com.agrimatch.admin.AdminUtil;
import com.agrimatch.common.api.Result;
import com.agrimatch.points.dto.AdminFulfillRequest;
import com.agrimatch.points.dto.AdminJdRedeemResponse;
import com.agrimatch.points.service.PointsService;
import com.agrimatch.user.mapper.UserMapper;
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

    @GetMapping
    public Result<List<AdminJdRedeemResponse>> list(
            Authentication authentication,
            @RequestParam(required = false) Integer status) {
        AdminUtil.requireAdmin(authentication, userMapper);
        return Result.success(pointsService.listAllJdRedeems(status));
    }

    @PostMapping("/{id}/fulfill")
    public Result<Void> fulfill(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AdminFulfillRequest req) {
        Long adminUserId = AdminUtil.requireAdmin(authentication, userMapper);
        pointsService.fulfillJdRedeem(adminUserId, id, req.getCardCode());
        return Result.success();
    }

    @PostMapping("/{id}/fail")
    public Result<Void> fail(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        Long adminUserId = AdminUtil.requireAdmin(authentication, userMapper);
        String remark = body != null ? body.get("remark") : null;
        pointsService.failJdRedeem(adminUserId, id, remark);
        return Result.success();
    }
}
