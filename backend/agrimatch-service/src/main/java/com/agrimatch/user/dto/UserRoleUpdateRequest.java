package com.agrimatch.user.dto;

import jakarta.validation.constraints.NotNull;

public class UserRoleUpdateRequest {
    @NotNull
    private Integer isBuyer;

    @NotNull
    private Integer isSeller;

    public Integer getIsBuyer() {
        return isBuyer;
    }

    public void setIsBuyer(Integer isBuyer) {
        this.isBuyer = isBuyer;
    }

    public Integer getIsSeller() {
        return isSeller;
    }

    public void setIsSeller(Integer isSeller) {
        this.isSeller = isSeller;
    }
}


