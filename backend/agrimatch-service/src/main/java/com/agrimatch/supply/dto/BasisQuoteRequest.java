package com.agrimatch.supply.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 基差报价请求（单条）
 */
public class BasisQuoteRequest {
    @NotBlank
    private String contractCode;      // 期货合约代码 (M2509)

    @NotNull
    private BigDecimal basisPrice;    // 基差

    @NotNull
    private BigDecimal availableQty;  // 可售量

    private String remark;

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public BigDecimal getBasisPrice() {
        return basisPrice;
    }

    public void setBasisPrice(BigDecimal basisPrice) {
        this.basisPrice = basisPrice;
    }

    public BigDecimal getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(BigDecimal availableQty) {
        this.availableQty = availableQty;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

