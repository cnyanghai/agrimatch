package com.agrimatch.supply.dto;

public class SupplyQuery {
    private Long companyId;
    private Long userId;
    private String categoryName;
    private String domain;
    /**
     * 状态（0发布中 1部分成交 2已下架 3全部成交）
     * - 不传：不按状态过滤（用于管理端“全部状态”）
     */
    private Integer status;

    /**
     * 是否只展示“有效供应”（用于大厅/浏览页）
     * - true：仅展示 status IN (0,1)
     * - false/null：不启用该过滤
     */
    private Boolean activeOnly;

    /**
     * 仅我的发布：是否包含过期（默认不包含）
     * - null/false：过期自动撤下并不在“上架列表”中展示
     * - true：允许查看过期记录（通常配合 status=2）
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getActiveOnly() {
        return activeOnly;
    }

    public void setActiveOnly(Boolean activeOnly) {
        this.activeOnly = activeOnly;
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


