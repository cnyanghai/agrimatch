package com.agrimatch.points.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.points.dto.*;
import com.agrimatch.points.service.PointsService;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    // ================= 新增：充值订单 API =================

    /**
     * 创建充值订单（返回支付二维码）
     */
    @PostMapping("/recharge/create")
    public Result<RechargeCreateResponse> createRechargeOrder(
            Authentication authentication,
            @Valid @RequestBody RechargeCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(pointsService.createRechargeOrder(userId, req));
    }

    /**
     * 查询充值订单状态
     */
    @GetMapping("/recharge/{orderNo}/status")
    public Result<Integer> getRechargeOrderStatus(@PathVariable String orderNo) {
        return Result.success(pointsService.getRechargeOrderStatus(orderNo));
    }

    /**
     * 模拟支付回调（开发测试用）
     */
    @PostMapping("/recharge/{orderNo}/confirm")
    public Result<Void> confirmRechargeOrder(
            @PathVariable String orderNo,
            @RequestParam(required = false) String tradeNo) {
        pointsService.confirmRechargeOrder(orderNo, tradeNo);
        return Result.success();
    }

    // ================= 新增：京东卡兑换 API =================

    /**
     * 兑换京东购物卡
     */
    @PostMapping("/redeem/jd")
    public Result<JdRedeemResponse> redeemJdCard(
            Authentication authentication,
            @Valid @RequestBody JdRedeemRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(pointsService.redeemJdCard(userId, req));
    }

    /**
     * 获取限额信息
     */
    @GetMapping("/limits")
    public Result<PointsLimitsResponse> getLimits(Authentication authentication) {
        Long userId = null;
        try {
            userId = SecurityUtil.requireUserId(authentication);
        } catch (Exception ignored) {}
        return Result.success(pointsService.getLimits(userId));
    }
}


