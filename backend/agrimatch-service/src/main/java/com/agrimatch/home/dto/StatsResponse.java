package com.agrimatch.home.dto;

public class StatsResponse {
    private Long userCount;
    private Long requirementCount;
    private Long supplyCount;
    private Long supplierCount;
    private Long buyerCount;

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public Long getRequirementCount() {
        return requirementCount;
    }

    public void setRequirementCount(Long requirementCount) {
        this.requirementCount = requirementCount;
    }

    public Long getSupplyCount() {
        return supplyCount;
    }

    public void setSupplyCount(Long supplyCount) {
        this.supplyCount = supplyCount;
    }

    public Long getSupplierCount() {
        return supplierCount;
    }

    public void setSupplierCount(Long supplierCount) {
        this.supplierCount = supplierCount;
    }

    public Long getBuyerCount() {
        return buyerCount;
    }

    public void setBuyerCount(Long buyerCount) {
        this.buyerCount = buyerCount;
    }
}


