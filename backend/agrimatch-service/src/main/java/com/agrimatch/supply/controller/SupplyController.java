package com.agrimatch.supply.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.supply.dto.SupplyCreateRequest;
import com.agrimatch.supply.dto.SupplyQuery;
import com.agrimatch.supply.dto.SupplyResponse;
import com.agrimatch.supply.dto.SupplyUpdateRequest;
import com.agrimatch.supply.service.SupplyService;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/supplies")
@Validated
public class SupplyController {
    private final SupplyService supplyService;

    public SupplyController(SupplyService supplyService) {
        this.supplyService = supplyService;
    }

    @PostMapping
    public Result<Long> create(Authentication authentication, @Valid @RequestBody SupplyCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(supplyService.create(userId, req));
    }

    /**
     * 获取下一条供应编号/单号（用于“预览/开单”）
     */
    @GetMapping("/next-no")
    public Result<String> nextNo(Authentication authentication) {
        SecurityUtil.requireUserId(authentication);
        return Result.success(com.agrimatch.util.NoUtil.gen("GY"));
    }

    @GetMapping("/{id}")
    public Result<SupplyResponse> getById(@PathVariable("id") @NotNull Long id) {
        return Result.success(supplyService.getById(id));
    }

    @GetMapping
    public Result<List<SupplyResponse>> list(Authentication authentication, SupplyQuery query) {
        // 允许匿名访问（大厅页面）
        Long userId = SecurityUtil.getUserIdOrNull(authentication);
        return Result.success(supplyService.list(userId, query));
    }

    @PutMapping("/{id}")
    public Result<Void> update(Authentication authentication,
                              @PathVariable("id") @NotNull Long id,
                              @Valid @RequestBody SupplyUpdateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        supplyService.update(userId, id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        supplyService.delete(userId, id);
        return Result.success();
    }
}


