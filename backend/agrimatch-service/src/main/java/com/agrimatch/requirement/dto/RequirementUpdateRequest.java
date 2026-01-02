package com.agrimatch.requirement.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RequirementUpdateRequest {
    @Size(max = 64)
    private String categoryName;

    private BigDecimal quantity;

    private BigDecimal expectedPrice;

    @Size(max = 20)
    private String packaging;

    @Size(max = 20)
    private String invoiceType;

    @Size(max = 20)
    private String paymentMethod;

    @Size(max = 20)
    private String deliveryMethod;

    private String paramsJson;

    @Size(max = 255)
    private String remark;

    /**
     * 发布有效期（分钟）；空/<=0 表示长期有效（后端会做上限保护）
     */
    private Integer expireMinutes;

    private BigDecimal purchaseLat;
    private BigDecimal purchaseLng;

    @Size(max = 255)
    private String purchaseAddress;

    private Integer status;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getExpectedPrice() {
        return expectedPrice;
    }

    public void setExpectedPrice(BigDecimal expectedPrice) {
        this.expectedPrice = expectedPrice;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
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

    public BigDecimal getPurchaseLat() {
        return purchaseLat;
    }

    public void setPurchaseLat(BigDecimal purchaseLat) {
        this.purchaseLat = purchaseLat;
    }

    public BigDecimal getPurchaseLng() {
        return purchaseLng;
    }

    public void setPurchaseLng(BigDecimal purchaseLng) {
        this.purchaseLng = purchaseLng;
    }

    public String getPurchaseAddress() {
        return purchaseAddress;
    }

    public void setPurchaseAddress(String purchaseAddress) {
        this.purchaseAddress = purchaseAddress;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}


