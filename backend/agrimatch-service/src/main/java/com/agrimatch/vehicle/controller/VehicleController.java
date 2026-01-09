package com.agrimatch.vehicle.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.util.SecurityUtil;
import com.agrimatch.vehicle.dto.VehicleCreateRequest;
import com.agrimatch.vehicle.dto.VehicleResponse;
import com.agrimatch.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@Validated
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * 获取当前公司的车辆列表
     */
    @GetMapping
    public Result<List<VehicleResponse>> list(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(vehicleService.listByCompany(userId));
    }

    /**
     * 获取车辆详情
     */
    @GetMapping("/{id}")
    public Result<VehicleResponse> getById(@PathVariable("id") @NotNull Long id) {
        return Result.success(vehicleService.getById(id));
    }

    /**
     * 添加常用车辆
     */
    @PostMapping
    public Result<Long> create(Authentication authentication, 
                                @Valid @RequestBody VehicleCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(vehicleService.create(userId, req));
    }

    /**
     * 修改车辆信息
     */
    @PutMapping("/{id}")
    public Result<Void> update(Authentication authentication,
                                @PathVariable("id") @NotNull Long id,
                                @Valid @RequestBody VehicleCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        vehicleService.update(userId, id, req);
        return Result.success();
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication authentication,
                                @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        vehicleService.delete(userId, id);
        return Result.success();
    }

    /**
     * 设为默认车辆
     */
    @PostMapping("/{id}/default")
    public Result<Void> setDefault(Authentication authentication,
                                    @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        vehicleService.setDefault(userId, id);
        return Result.success();
    }
}

