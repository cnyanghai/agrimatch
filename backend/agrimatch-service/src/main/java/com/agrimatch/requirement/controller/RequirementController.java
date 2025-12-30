package com.agrimatch.requirement.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.requirement.dto.RequirementCreateRequest;
import com.agrimatch.requirement.dto.RequirementQuery;
import com.agrimatch.requirement.dto.RequirementResponse;
import com.agrimatch.requirement.dto.RequirementUpdateRequest;
import com.agrimatch.requirement.service.RequirementService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/requirements")
@Validated
public class RequirementController {
    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @PostMapping
    public Result<Long> create(Authentication authentication, @Valid @RequestBody RequirementCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(requirementService.create(userId, req));
    }

    /**
     * 获取下一条合同号/单号（用于“开单/预览”）
     */
    @GetMapping("/next-no")
    public Result<String> nextNo(Authentication authentication) {
        SecurityUtil.requireUserId(authentication);
        return Result.success(com.agrimatch.util.NoUtil.gen("CG"));
    }

    @GetMapping("/{id}")
    public Result<RequirementResponse> getById(@PathVariable("id") @NotNull Long id) {
        return Result.success(requirementService.getById(id));
    }

    @GetMapping
    public Result<List<RequirementResponse>> list(
            Authentication authentication,
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "categoryName", required = false) String categoryName,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "orderBy", required = false) String orderBy,
            @RequestParam(value = "order", required = false) String order
    ) {
        Long viewerUserId = SecurityUtil.requireUserId(authentication);
        RequirementQuery q = new RequirementQuery();
        q.setCompanyId(companyId);
        q.setUserId(userId);
        q.setCategoryName(categoryName);
        q.setStatus(status);
        q.setOrderBy(orderBy);
        q.setOrder(order);
        return Result.success(requirementService.list(viewerUserId, q));
    }

    @PutMapping("/{id}")
    public Result<Void> update(Authentication authentication,
                               @PathVariable("id") @NotNull Long id,
                               @Valid @RequestBody RequirementUpdateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        requirementService.update(userId, id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        requirementService.delete(userId, id);
        return Result.success();
    }
}


