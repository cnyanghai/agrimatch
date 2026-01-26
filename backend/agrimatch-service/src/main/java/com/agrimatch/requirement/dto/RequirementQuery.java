package com.agrimatch.requirement.dto;

public class RequirementQuery {
    private Long companyId;
    private Long userId;
    private String categoryName;
    private String domain;
    private Integer status;
    /**
     * 是否包含已过期数据：
     * - true：管理端可查看（用于“已发布列表/历史”）
     * - false/null：默认只展示未过期（用于大厅浏览）
     */
    private Boolean includeExpired;
    private String orderBy;
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


