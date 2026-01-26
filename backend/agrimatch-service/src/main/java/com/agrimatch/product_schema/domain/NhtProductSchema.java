package com.agrimatch.product_schema.domain;

import java.time.LocalDateTime;

/**
 * 产品业态实体
 */
public class NhtProductSchema {
    private Long id;
    private String schemaCode;      // 业态代码：feed, poultry, meat, other
    private String schemaName;      // 业态名称
    private String description;     // 业态描述
    private String formConfigJson;  // 表单布局配置JSON
    private String icon;            // 图标
    private Integer sort;           // 排序
    private String status;          // 状态：0正常 1停用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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

    public String getFormConfigJson() {
        return formConfigJson;
    }

    public void setFormConfigJson(String formConfigJson) {
        this.formConfigJson = formConfigJson;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
