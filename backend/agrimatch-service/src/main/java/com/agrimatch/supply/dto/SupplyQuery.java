package com.agrimatch.supply.dto;

public class SupplyQuery {
    private Long companyId;
    private Long userId;
    private String categoryName;
    /**
     * 状态（0上架 1下架）
     * - 不传：后端默认只展示上架(0)
     */
    private Integer status;

    /**
     * 仅我的发布：是否包含过期（默认不包含）
     * - null/false：过期自动撤下并不在“上架列表”中展示
     * - true：允许查看过期记录（通常配合 status=1）
     */
    private Boolean includeExpired;

    /**
     * 支持：create_time / ex_factory_price / distance(占位) / delivered_price(占位)
     */
    private String orderBy;

    /**
     * asc / desc
     */
    private String order;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getIncludeExpired() {
        return includeExpired;
    }

    public void setIncludeExpired(Boolean includeExpired) {
        this.includeExpired = includeExpired;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}


