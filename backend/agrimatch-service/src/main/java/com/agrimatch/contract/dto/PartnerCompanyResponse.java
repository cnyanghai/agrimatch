package com.agrimatch.contract.dto;

import java.math.BigDecimal;

/**
 * 合作商家响应
 */
public class PartnerCompanyResponse {

    /** 公司ID */
    private Long companyId;

    /** 公司名称 */
    private String companyName;

    /** 合同数 */
    private Integer contractCount;

    /** 累计金额 */
    private BigDecimal totalAmount;

    public PartnerCompanyResponse() {}

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getContractCount() {
        return contractCount;
    }

    public void setContractCount(Integer contractCount) {
        this.contractCount = contractCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
