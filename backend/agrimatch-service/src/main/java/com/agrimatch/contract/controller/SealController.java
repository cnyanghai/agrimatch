package com.agrimatch.contract.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.contract.domain.BusCompanySeal;
import com.agrimatch.contract.dto.SealCreateRequest;
import com.agrimatch.contract.dto.SealResponse;
import com.agrimatch.contract.service.SealService;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 公司电子章管理
 */
@RestController
@RequestMapping("/api/seals")
@Validated
public class SealController {
    
    private final SealService sealService;

    public SealController(SealService sealService) {
        this.sealService = sealService;
    }

    /**
     * 创建电子章（上传或生成）
     */
    @PostMapping
    public Result<Long> create(Authentication authentication, @Valid @RequestBody SealCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        Long id;
        if (req.isGenerate()) {
            id = sealService.generateSeal(userId, req.getSealName(), req.getSealType());
        } else {
            id = sealService.uploadSeal(userId, req.getSealName(), req.getSealType(), req.getSealUrl());
        }
        return Result.success(id);
    }

    /**
     * 获取公司的所有电子章
     */
    @GetMapping
    public Result<List<SealResponse>> list(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        List<BusCompanySeal> seals = sealService.listByCompany(userId);
        List<SealResponse> out = new ArrayList<>();
        for (BusCompanySeal s : seals) {
            out.add(toResponse(s));
        }
        return Result.success(out);
    }

    /**
     * 获取默认电子章
     */
    @GetMapping("/default")
    public Result<SealResponse> getDefault(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        BusCompanySeal seal = sealService.getDefault(userId);
        return Result.success(seal != null ? toResponse(seal) : null);
    }

    /**
     * 设置默认电子章
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        sealService.setDefault(userId, id);
        return Result.success();
    }

    /**
     * 删除电子章
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        sealService.delete(userId, id);
        return Result.success();
    }

    private static SealResponse toResponse(BusCompanySeal s) {
        SealResponse r = new SealResponse();
        r.setId(s.getId());
        r.setCompanyId(s.getCompanyId());
        r.setSealName(s.getSealName());
        r.setSealType(s.getSealType());
        r.setSealUrl(s.getSealUrl());
        r.setIsGenerated(s.getIsGenerated());
        r.setIsDefault(s.getIsDefault());
        r.setCreateTime(s.getCreateTime());
        return r;
    }
}

