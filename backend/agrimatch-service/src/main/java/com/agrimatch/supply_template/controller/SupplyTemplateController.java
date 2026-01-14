package com.agrimatch.supply_template.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.supply_template.dto.SupplyTemplateCreateRequest;
import com.agrimatch.supply_template.dto.SupplyTemplateResponse;
import com.agrimatch.supply_template.service.SupplyTemplateService;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/supply-templates")
@Validated
public class SupplyTemplateController {
    private final SupplyTemplateService service;

    public SupplyTemplateController(SupplyTemplateService service) {
        this.service = service;
    }

    @PostMapping
    public Result<Long> create(Authentication authentication, @Valid @RequestBody SupplyTemplateCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(service.create(userId, req));
    }

    @GetMapping
    public Result<List<SupplyTemplateResponse>> listMy(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(service.listMy(userId));
    }

    @GetMapping("/{id}")
    public Result<SupplyTemplateResponse> getMy(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(service.getMyById(userId, id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        service.delete(userId, id);
        return Result.success();
    }
}

