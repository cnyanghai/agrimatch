package com.agrimatch.product_schema.service.impl;

import com.agrimatch.product.mapper.ProductMapper;
import com.agrimatch.product_schema.domain.NhtProductSchema;
import com.agrimatch.product_schema.dto.ProductSchemaVO;
import com.agrimatch.product_schema.dto.ProductSchemaVO.CategoryNode;
import com.agrimatch.product_schema.mapper.ProductSchemaMapper;
import com.agrimatch.product_schema.service.ProductSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductSchemaServiceImpl implements ProductSchemaService {

    @Autowired
    private ProductSchemaMapper schemaMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<NhtProductSchema> listAll() {
        return schemaMapper.selectAllActive();
    }

    @Override
    public NhtProductSchema getByCode(String schemaCode) {
        return schemaMapper.selectByCode(schemaCode);
    }

    @Override
    public List<ProductSchemaVO> getSchemaWithCategories() {
        // 1. 获取所有业态
        List<NhtProductSchema> schemas = schemaMapper.selectAllActive();

        // 2. 获取所有产品分类
        List<Map<String, Object>> allProducts = productMapper.selectAllWithSchema();

        // 3. 按业态分组
        Map<String, List<Map<String, Object>>> productsBySchema = allProducts.stream()
                .collect(Collectors.groupingBy(p -> (String) p.get("schema_code")));

        // 4. 构建结果
        List<ProductSchemaVO> result = new ArrayList<>();
        for (NhtProductSchema schema : schemas) {
            ProductSchemaVO vo = new ProductSchemaVO();
            vo.setId(schema.getId());
            vo.setSchemaCode(schema.getSchemaCode());
            vo.setSchemaName(schema.getSchemaName());
            vo.setDescription(schema.getDescription());
            vo.setIcon(schema.getIcon());
            vo.setSort(schema.getSort());

            // 构建该业态的分类树
            List<Map<String, Object>> schemaProducts = productsBySchema.getOrDefault(schema.getSchemaCode(), new ArrayList<>());
            vo.setCategories(buildCategoryTree(schemaProducts));

            result.add(vo);
        }

        return result;
    }

    @Override
    public ProductSchemaVO getSchemaWithCategoriesByCode(String schemaCode) {
        NhtProductSchema schema = schemaMapper.selectByCode(schemaCode);
        if (schema == null) {
            return null;
        }

        ProductSchemaVO vo = new ProductSchemaVO();
        vo.setId(schema.getId());
        vo.setSchemaCode(schema.getSchemaCode());
        vo.setSchemaName(schema.getSchemaName());
        vo.setDescription(schema.getDescription());
        vo.setIcon(schema.getIcon());
        vo.setSort(schema.getSort());

        // 获取该业态的产品分类
        List<Map<String, Object>> products = productMapper.selectBySchemaCode(schemaCode);
        vo.setCategories(buildCategoryTree(products));

        return vo;
    }

    /**
     * 构建分类树
     */
    private List<CategoryNode> buildCategoryTree(List<Map<String, Object>> products) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为节点
        List<CategoryNode> nodes = products.stream().map(p -> {
            CategoryNode node = new CategoryNode();
            node.setId(((Number) p.get("id")).longValue());
            Object parentId = p.get("parent_id");
            node.setParentId(parentId != null ? ((Number) parentId).longValue() : 0L);
            node.setName((String) p.get("product_name"));
            Object hasParams = p.get("has_params");
            node.setHasParams(hasParams != null && ((Number) hasParams).intValue() == 1);
            Object allowCustomName = p.get("allow_custom_name");
            node.setAllowCustomName(allowCustomName != null && ((Number) allowCustomName).intValue() == 1);
            node.setChildren(new ArrayList<>());
            return node;
        }).collect(Collectors.toList());

        // 按 parentId 分组
        Map<Long, List<CategoryNode>> byParent = nodes.stream()
                .collect(Collectors.groupingBy(CategoryNode::getParentId));

        // 找出根节点（parentId = 0）并构建树
        List<CategoryNode> roots = new ArrayList<>();
        for (CategoryNode node : nodes) {
            if (node.getParentId() == 0L) {
                // 根节点，填充子节点
                node.setChildren(byParent.getOrDefault(node.getId(), new ArrayList<>()));
                roots.add(node);
            }
        }

        // 如果没有根节点，可能所有节点都是叶子节点
        if (roots.isEmpty()) {
            return nodes;
        }

        return roots;
    }
}
