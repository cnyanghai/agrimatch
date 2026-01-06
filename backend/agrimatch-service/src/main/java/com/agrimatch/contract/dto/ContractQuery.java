package com.agrimatch.contract.dto;

public class ContractQuery {
    private Long companyId;         // 查询买方或卖方公司的合同
    private Long buyerCompanyId;    // 查询买方公司的合同
    private Long sellerCompanyId;   // 查询卖方公司的合同
    private String keyword;
    private Integer status;
    private String orderBy;
    private String order;

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getBuyerCompanyId() { return buyerCompanyId; }
    public void setBuyerCompanyId(Long buyerCompanyId) { this.buyerCompanyId = buyerCompanyId; }

    public Long getSellerCompanyId() { return sellerCompanyId; }
    public void setSellerCompanyId(Long sellerCompanyId) { this.sellerCompanyId = sellerCompanyId; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getOrderBy() { return orderBy; }
    public void setOrderBy(String orderBy) { this.orderBy = orderBy; }

    public String getOrder() { return order; }
    public void setOrder(String order) { this.order = order; }
}
