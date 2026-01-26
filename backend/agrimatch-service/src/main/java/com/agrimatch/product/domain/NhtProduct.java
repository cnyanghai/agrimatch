package com.agrimatch.product.domain;

import java.time.LocalDateTime;

public class NhtProduct {
    private Long id;
    private Long userId;
    private Long parentId;
    private String productName;
    private String schemaCode;      // 所属业态：feed, poultry, meat, other
    private Integer hasParams;      // 是否有预设参数：0否 1是
    private Integer allowCustomName; // 是否允许自定义名称：0否 1是
    private String status;
    private String delFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSchemaCode() {
        return schemaCode;
    }

    public void setSchemaCode(String schemaCode) {
        this.schemaCode = schemaCode;
    }

    public Integer getHasParams() {
        return hasParams;
    }

    public void setHasParams(Integer hasParams) {
        this.hasParams = hasParams;
    }

    public Integer getAllowCustomName() {
        return allowCustomName;
    }

    public void setAllowCustomName(Integer allowCustomName) {
        this.allowCustomName = allowCustomName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
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


