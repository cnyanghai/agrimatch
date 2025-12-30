package com.agrimatch.deal.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DealCreateRequest {
    @NotNull
    private Long requirementId;

    @NotNull
    private Long supplyId;

    @NotNull
    private BigDecimal quantity;

    /**
     * 成交出厂价（可选，不传则用供应的出厂价）
     */
    private BigDecimal finalExFactoryPrice;

    /**
     * 交付方式（可选，不传则用供应的 deliveryMode）
     */
    private String deliveryMode;

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
}


