package com.agrimatch.product.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.product.dto.ProductCreateRequest;
import com.agrimatch.product.dto.ProductNode;
import com.agrimatch.product.service.ProductService;
import com.agrimatch.product_param.dto.ProductParamResponse;
import com.agrimatch.product_param.dto.ProductParamOptionAddRequest;
import com.agrimatch.product_param.service.ProductParamService;
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
@RequestMapping("/api/products")
@Validated
public class ProductController {
    private final ProductService productService;
    private final ProductParamService productParamService;

    public ProductController(ProductService productService, ProductParamService productParamService) {
        this.productService = productService;
        this.productParamService = productParamService;
    }

    @GetMapping("/tree")
    public Result<List<ProductNode>> tree() {
        return Result.success(productService.tree());
    }

    @GetMapping("/search")
    public Result<List<ProductNode>> search(@RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(productService.search(keyword));
    }

    @PostMapping("/custom")
    public Result<Long> createCustom(Authentication authentication, @Valid @RequestBody ProductCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(productService.createCustom(userId, req));
    }

    @GetMapping("/{productId}/params")
    public Result<List<ProductParamResponse>> params(@PathVariable("productId") @NotNull Long productId) {
        return Result.success(productParamService.listByProductId(productId));
    }

    @PostMapping("/params/{paramId}/options")
    public Result<List<String>> addParamOption(Authentication authentication,
                                              @PathVariable("paramId") @NotNull Long paramId,
                                              @Valid @RequestBody ProductParamOptionAddRequest req) {
        SecurityUtil.requireUserId(authentication);
        return Result.success(productParamService.addOption(paramId, req.getOption()));
    }
}


