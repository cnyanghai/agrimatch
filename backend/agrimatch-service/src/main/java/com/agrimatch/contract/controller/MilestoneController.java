package com.agrimatch.contract.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.contract.dto.MilestoneCreateRequest;
import com.agrimatch.contract.dto.MilestoneResponse;
import com.agrimatch.contract.dto.MilestoneSubmitRequest;
import com.agrimatch.contract.service.MilestoneService;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 合同履约节点管理
 */
@RestController
@RequestMapping("/api/contracts/{contractId}/milestones")
@Validated
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    /**
     * 添加履约节点
     */
    @PostMapping
    public Result<Long> create(Authentication authentication,
                               @PathVariable("contractId") @NotNull Long contractId,
                               @Valid @RequestBody MilestoneCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(milestoneService.create(userId, contractId, req));
    }

    /**
     * 获取合同的所有履约节点
     */
    @GetMapping
    public Result<List<MilestoneResponse>> list(Authentication authentication,
                                                 @PathVariable("contractId") @NotNull Long contractId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(milestoneService.listByContract(userId, contractId));
    }

    /**
     * 提交履约节点（上传凭证）
     */
    @PostMapping("/{milestoneId}/submit")
    public Result<Void> submit(Authentication authentication,
                               @PathVariable("contractId") @NotNull Long contractId,
                               @PathVariable("milestoneId") @NotNull Long milestoneId,
                               @Valid @RequestBody MilestoneSubmitRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        milestoneService.submit(userId, milestoneId, req);
        return Result.success();
    }

    /**
     * 确认履约节点
     */
    @PostMapping("/{milestoneId}/confirm")
    public Result<Void> confirm(Authentication authentication,
                                @PathVariable("contractId") @NotNull Long contractId,
                                @PathVariable("milestoneId") @NotNull Long milestoneId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        milestoneService.confirm(userId, milestoneId);
        return Result.success();
    }

    /**
     * 拒绝履约节点
     */
    @PostMapping("/{milestoneId}/reject")
    public Result<Void> reject(Authentication authentication,
                               @PathVariable("contractId") @NotNull Long contractId,
                               @PathVariable("milestoneId") @NotNull Long milestoneId,
                               @RequestParam(name = "reason", required = false) String reason) {
        Long userId = SecurityUtil.requireUserId(authentication);
        milestoneService.reject(userId, milestoneId, reason);
        return Result.success();
    }

    /**
     * 删除履约节点
     */
    @DeleteMapping("/{milestoneId}")
    public Result<Void> delete(Authentication authentication,
                               @PathVariable("contractId") @NotNull Long contractId,
                               @PathVariable("milestoneId") @NotNull Long milestoneId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        milestoneService.delete(userId, milestoneId);
        return Result.success();
    }
}

