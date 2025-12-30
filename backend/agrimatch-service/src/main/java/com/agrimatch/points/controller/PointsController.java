package com.agrimatch.points.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.points.dto.PointsMeResponse;
import com.agrimatch.points.dto.PointsRechargeRequest;
import com.agrimatch.points.dto.PointsRedeemRequest;
import com.agrimatch.points.dto.PointsTxResponse;
import com.agrimatch.points.service.PointsService;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@Validated
public class PointsController {
    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping("/me")
    public Result<PointsMeResponse> me(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(pointsService.me(userId));
    }

    @PostMapping("/recharge")
    public Result<PointsMeResponse> recharge(Authentication authentication, @Valid @RequestBody PointsRechargeRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(pointsService.recharge(userId, req.getPoints()));
    }

    @PostMapping("/redeem")
    public Result<PointsMeResponse> redeem(Authentication authentication, @Valid @RequestBody PointsRedeemRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(pointsService.redeem(userId, req.getPoints()));
    }

    @GetMapping("/tx")
    public Result<List<PointsTxResponse>> myTx(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(pointsService.myTx(userId));
    }
}


