package com.agrimatch.supply.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SupplyResponse {
    private Long id;
    private Long companyId;
    private Long userId;
    private String companyName;
    private String userName;
    private String nickName;
    private String categoryName;
    private String domain;
    private String supplyNo;
    private String origin;
    private BigDecimal quantity;

    /**
     * 报价类型：0=现货一口价，1=基差报价
     */
    private Integer priceType;

    private BigDecimal exFactoryPrice;

    /**
     * 基差报价明细（priceType=1 时有值）
     */
    private List<BasisQuoteResponse> basisQuotes;
    private String shipAddress;
    private String deliveryMode;
    private String paymentMethod;
    private String invoiceType;
    private String packaging;
    private String storageMethod;
    private String priceRulesJson;
    private String paramsJson;
    private String tagsJson;
    /**
     * 状态（0发布中 1部分成交 2已下架 3全部成交）
     */
    private Integer status;
    /**
     * 剩余数量（支持多次成交）：remaining = quantity - sum(deal.quantity)
     * - 若 quantity 为空，则 remaining 为空
     */
    private BigDecimal remainingQuantity;
    private Integer expireMinutes;
    private LocalDateTime expireTime;
    private String remark;
    /**
     * 距离（公里），后端按“当前用户公司坐标 -> 供应公司坐标”估算，可能为空
     */
    private BigDecimal distanceKm;
    /**
     * 到厂价（单价），后端按 deliveredPrice = exFactoryPrice + distanceKm * ratePerTonKm 估算，可能为空
     */
    private BigDecimal deliveredPrice;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
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

    public String getSupplyNo() {
        return supplyNo;
    }

    public void setSupplyNo(String supplyNo) {
        this.supplyNo = supplyNo;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getExFactoryPrice() {
        return exFactoryPrice;
    }

    public void setExFactoryPrice(BigDecimal exFactoryPrice) {
        this.exFactoryPrice = exFactoryPrice;
    }

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public List<BasisQuoteResponse> getBasisQuotes() {
        return basisQuotes;
    }

    public void setBasisQuotes(List<BasisQuoteResponse> basisQuotes) {
        this.basisQuotes = basisQuotes;
    }

    public String getShipAddress() {
        return shipAddress;
    }

    public void setShipAddress(String shipAddress) {
        this.shipAddress = shipAddress;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getStorageMethod() {
        return storageMethod;
    }

    public void setStorageMethod(String storageMethod) {
        this.storageMethod = storageMethod;
    }

    public String getPriceRulesJson() {
        return priceRulesJson;
    }

    public void setPriceRulesJson(String priceRulesJson) {
        this.priceRulesJson = priceRulesJson;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public String getTagsJson() {
        return tagsJson;
    }

    public void setTagsJson(String tagsJson) {
        this.tagsJson = tagsJson;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(BigDecimal remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public Integer getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(Integer expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getDeliveredPrice() {
        return deliveredPrice;
    }

    public void setDeliveredPrice(BigDecimal deliveredPrice) {
        this.deliveredPrice = deliveredPrice;
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


