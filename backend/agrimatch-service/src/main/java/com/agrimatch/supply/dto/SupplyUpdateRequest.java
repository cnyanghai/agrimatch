package com.agrimatch.supply.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class SupplyUpdateRequest {
    @Size(max = 64)
    private String categoryName;

    @Size(max = 128)
    private String origin;

    private BigDecimal quantity;

    private BigDecimal exFactoryPrice;

    @Size(max = 255)
    private String shipAddress;

    @Size(max = 20)
    private String deliveryMode;

    @Size(max = 20)
    private String paymentMethod;

    @Size(max = 50)
    private String invoiceType;

    @Size(max = 20)
    private String packaging;

    @Size(max = 20)
    private String storageMethod;

    private String priceRulesJson;

    private String domain;
    private String tagsJson;

    private String paramsJson;

    @Size(max = 500)
    private String remark;

    /**
     * 发布有效期（分钟）；空/<=0 表示长期有效（后端会做上限保护）
     */
    private Integer expireMinutes;

    /**
     * 状态（0发布中 1部分成交 2已下架 3全部成交）
     */
    private Integer status;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTagsJson() {
        return tagsJson;
    }

    public void setTagsJson(String tagsJson) {
        this.tagsJson = tagsJson;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(Integer expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}


