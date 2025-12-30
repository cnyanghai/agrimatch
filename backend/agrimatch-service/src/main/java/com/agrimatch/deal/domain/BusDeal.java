package com.agrimatch.deal.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BusDeal {
    private Long id;
    private Long requirementId;
    private Long supplyId;
    private Long buyerCompanyId;
    private Long sellerCompanyId;
    private Long buyerUserId;
    private Long sellerUserId;
    private BigDecimal quantity;
    private BigDecimal finalExFactoryPrice;
    private String deliveryMode;
    private BigDecimal distanceKm;
    private BigDecimal freightRatePerTonKm;
    private BigDecimal deliveredPrice;
    private Integer status;
    private String remark;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public Long getSupplyId() {
        return supplyId;
    }

    public void setSupplyId(Long supplyId) {
        this.supplyId = supplyId;
    }

    public Long getBuyerCompanyId() {
        return buyerCompanyId;
    }

    public void setBuyerCompanyId(Long buyerCompanyId) {
        this.buyerCompanyId = buyerCompanyId;
    }

    public Long getSellerCompanyId() {
        return sellerCompanyId;
    }

    public void setSellerCompanyId(Long sellerCompanyId) {
        this.sellerCompanyId = sellerCompanyId;
    }

    public Long getBuyerUserId() {
        return buyerUserId;
    }

    public void setBuyerUserId(Long buyerUserId) {
        this.buyerUserId = buyerUserId;
    }

    public Long getSellerUserId() {
        return sellerUserId;
    }

    public void setSellerUserId(Long sellerUserId) {
        this.sellerUserId = sellerUserId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getFinalExFactoryPrice() {
        return finalExFactoryPrice;
    }

    public void setFinalExFactoryPrice(BigDecimal finalExFactoryPrice) {
        this.finalExFactoryPrice = finalExFactoryPrice;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getFreightRatePerTonKm() {
        return freightRatePerTonKm;
    }

    public void setFreightRatePerTonKm(BigDecimal freightRatePerTonKm) {
        this.freightRatePerTonKm = freightRatePerTonKm;
    }

    public BigDecimal getDeliveredPrice() {
        return deliveredPrice;
    }

    public void setDeliveredPrice(BigDecimal deliveredPrice) {
        this.deliveredPrice = deliveredPrice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
}


