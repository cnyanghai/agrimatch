package com.agrimatch.product_param.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductParamOptionAddRequest {
    @NotBlank
    @Size(max = 64)
    private String option;

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}


