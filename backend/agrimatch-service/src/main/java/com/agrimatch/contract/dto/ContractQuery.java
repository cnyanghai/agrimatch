package com.agrimatch.contract.dto;

public class ContractQuery {
    private Long companyId;
    private Long userId;
    private String keyword;
    private String status;
    private String orderBy;
    private String order;

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderBy() { return orderBy; }
    public void setOrderBy(String orderBy) { this.orderBy = orderBy; }

    public String getOrder() { return order; }
    public void setOrder(String order) { this.order = order; }
}


