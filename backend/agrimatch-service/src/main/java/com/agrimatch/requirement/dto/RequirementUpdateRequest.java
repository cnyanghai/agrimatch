package com.agrimatch.requirement.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RequirementUpdateRequest {
    @Size(max = 64)
    private String categoryName;

    private BigDecimal quantity;

    @Size(max = 20)
    private String packaging;

    @Size(max = 20)
    private String paymentMethod;

    private String paramsJson;

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

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
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


