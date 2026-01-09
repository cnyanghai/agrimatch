package com.agrimatch.supply.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应基差报价明细实体
 */
public class BusSupplyBasis {
    private Long id;
    private Long supplyId;
    private String contractCode;      // 期货合约代码 (M2509)
    private BigDecimal basisPrice;    // 基差（正=升水，负=贴水）
    private BigDecimal availableQty;  // 可售量
    private BigDecimal soldQty;       // 已售量
    private String remark;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联字段（从期货合约表join）
    private String contractName;      // 合约名称
    private BigDecimal lastPrice;     // 期货最新价格
    private BigDecimal referencePrice; // 参考现价 = lastPrice + basisPrice

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSupplyId() {
        return supplyId;
    }

    public void setSupplyId(Long supplyId) {
        this.supplyId = supplyId;
    }

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

    public BigDecimal getSoldQty() {
        return soldQty;
    }

    public void setSoldQty(BigDecimal soldQty) {
        this.soldQty = soldQty;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
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
}

