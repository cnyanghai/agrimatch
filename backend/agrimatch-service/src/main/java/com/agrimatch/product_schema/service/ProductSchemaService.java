package com.agrimatch.product_schema.service;

import com.agrimatch.product_schema.domain.NhtProductSchema;
import com.agrimatch.product_schema.dto.ProductSchemaVO;

import java.util.List;

public interface ProductSchemaService {

    /**
     * 获取所有业态列表
     */
    List<NhtProductSchema> listAll();

    /**
     * 根据业态代码查询
     */
    NhtProductSchema getByCode(String schemaCode);

    /**
     * 获取业态及其分类树（供发布页面和供求大厅使用）
     */
    List<ProductSchemaVO> getSchemaWithCategories();

    /**
     * 根据业态代码获取分类树
     */
    ProductSchemaVO getSchemaWithCategoriesByCode(String schemaCode);
}
