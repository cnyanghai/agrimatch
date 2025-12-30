package com.agrimatch.eval.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.eval.dto.EvalCreateRequest;
import com.agrimatch.eval.dto.EvalResponse;
import com.agrimatch.eval.service.EvalService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/evals")
@Validated
public class EvalController {
    private final EvalService evalService;

    public EvalController(EvalService evalService) {
        this.evalService = evalService;
    }

    @PostMapping
    public Result<Long> create(Authentication authentication, @Valid @RequestBody EvalCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(evalService.create(userId, req));
    }

    @GetMapping("/company/{companyId}")
    public Result<List<EvalResponse>> listByCompany(@PathVariable("companyId") @NotNull Long companyId) {
        return Result.success(evalService.listByCompany(companyId));
    }
}


