package com.agrimatch.deal.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.deal.dto.DealCreateRequest;
import com.agrimatch.deal.dto.DealResponse;
import com.agrimatch.deal.service.DealService;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
@Validated
public class DealController {
    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping
    public Result<Long> create(Authentication authentication, @Valid @RequestBody DealCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(dealService.create(userId, req));
    }

    @GetMapping("/{id}")
    public Result<DealResponse> getById(@PathVariable("id") @NotNull Long id) {
        return Result.success(dealService.getById(id));
    }

    @GetMapping
    public Result<List<DealResponse>> listByRequirement(@RequestParam("requirementId") @NotNull Long requirementId) {
        return Result.success(dealService.listByRequirement(requirementId));
    }
}


