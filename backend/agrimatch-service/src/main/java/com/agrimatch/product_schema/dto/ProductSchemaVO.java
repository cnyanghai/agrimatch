package com.agrimatch.product_schema.dto;

import java.util.List;

/**
 * 产品业态VO（包含分类树）
 */
public class ProductSchemaVO {
    private Long id;
    private String schemaCode;
    private String schemaName;
    private String description;
    private String icon;
    private Integer sort;
    private List<CategoryNode> categories;  // 该业态下的品类列表

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSchemaCode() {
        return schemaCode;
    }

    public void setSchemaCode(String schemaCode) {
        this.schemaCode = schemaCode;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public List<CategoryNode> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryNode> categories) {
        this.categories = categories;
    }

    /**
     * 品类节点
     */
    public static class CategoryNode {
        private Long id;
        private Long parentId;
        private String name;
        private Boolean hasParams;          // 是否有预设参数
        private Boolean allowCustomName;    // 是否允许自定义名称
        private List<CategoryNode> children;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getHasParams() {
            return hasParams;
        }

        public void setHasParams(Boolean hasParams) {
            this.hasParams = hasParams;
        }

        public Boolean getAllowCustomName() {
            return allowCustomName;
        }

        public void setAllowCustomName(Boolean allowCustomName) {
            this.allowCustomName = allowCustomName;
        }

        public List<CategoryNode> getChildren() {
            return children;
        }

        public void setChildren(List<CategoryNode> children) {
            this.children = children;
        }
    }
}
