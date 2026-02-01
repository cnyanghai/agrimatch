package com.agrimatch.points.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理员发卡请求
 */
public class AdminFulfillRequest {

    @NotBlank(message = "卡密不能为空")
    private String cardCode;

    public String getCardCode() { return cardCode; }
    public void setCardCode(String cardCode) { this.cardCode = cardCode; }
}
