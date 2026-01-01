package com.agrimatch.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SmsSendRequest {
    @NotBlank
    @Size(max = 20)
    private String phone;

    /**
     * 1-注册 2-登录 3-找回密码
     */
    @NotNull
    private Integer type;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}


