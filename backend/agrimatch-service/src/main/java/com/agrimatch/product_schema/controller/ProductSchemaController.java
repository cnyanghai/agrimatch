package com.agrimatch.product_schema.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.product_schema.domain.NhtProductSchema;
import com.agrimatch.product_schema.dto.ProductSchemaVO;
import com.agrimatch.product_schema.service.ProductSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品业态控制器
 */
@RestController
@RequestMapping("/api/product-schemas")
public class ProductSchemaController {

    @Autowired
    private ProductSchemaService schemaService;

    /**
     * 获取所有业态列表（不含分类树）
     */
    @GetMapping
    public Result<List<NhtProductSchema>> list() {
        return Result.success(schemaService.listAll());
    }

    /**
     * 获取业态及其分类树（供发布页面和供求大厅使用）
     */
    @GetMapping("/tree")
    public Result<List<ProductSchemaVO>> getSchemaTree() {
        return Result.success(schemaService.getSchemaWithCategories());
    }

    /**
     * 根据业态代码获取分类树
     */
    @GetMapping("/{schemaCode}")
    public Result<ProductSchemaVO> getByCode(@PathVariable("schemaCode") String schemaCode) {
        ProductSchemaVO vo = schemaService.getSchemaWithCategoriesByCode(schemaCode);
        if (vo == null) {
            return Result.fail(404, "业态不存在");
        }
        return Result.success(vo);
    }
}
