package com.agrimatch.supply.dto;

import java.math.BigDecimal;

/**
 * 基差报价响应（单条）
 */
public class BasisQuoteResponse {
    private Long id;
    private String contractCode;      // 期货合约代码 (M2509)
    private String contractName;      // 合约名称 (豆粕2509)
    private BigDecimal basisPrice;    // 基差
    private BigDecimal availableQty;  // 可售量
    private BigDecimal soldQty;       // 已售量
    private BigDecimal remainingQty;  // 剩余量 = availableQty - soldQty
    private BigDecimal lastPrice;     // 期货最新价格
    private BigDecimal referencePrice; // 参考现价 = lastPrice + basisPrice
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
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

    public BigDecimal getSoldQty() {
        return soldQty;
    }

    public void setSoldQty(BigDecimal soldQty) {
        this.soldQty = soldQty;
    }

    public BigDecimal getRemainingQty() {
        return remainingQty;
    }

    public void setRemainingQty(BigDecimal remainingQty) {
        this.remainingQty = remainingQty;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public BigDecimal getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(BigDecimal referencePrice) {
        this.referencePrice = referencePrice;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

