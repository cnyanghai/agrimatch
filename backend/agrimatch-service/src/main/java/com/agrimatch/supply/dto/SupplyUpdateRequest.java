package com.agrimatch.supply.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class SupplyUpdateRequest {
    @Size(max = 64)
    private String categoryName;

    private BigDecimal exFactoryPrice;

    @Size(max = 255)
    private String shipAddress;

    @Size(max = 20)
    private String deliveryMode;

    private String priceRulesJson;

    private String paramsJson;

    /**
     * 状态（0上架 1下架）
     */
    private Integer status;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}


