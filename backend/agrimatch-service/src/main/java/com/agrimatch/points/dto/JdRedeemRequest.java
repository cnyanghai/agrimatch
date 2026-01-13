package com.agrimatch.points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 京东卡兑换请求
 */
public class JdRedeemRequest {

    @NotNull(message = "面额不能为空")
    private Integer faceValue; // 50 / 100 / 200 / 500

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    public Integer getFaceValue() { return faceValue; }
    public void setFaceValue(Integer faceValue) { this.faceValue = faceValue; }

    public String getSmsCode() { return smsCode; }
    public void setSmsCode(String smsCode) { this.smsCode = smsCode; }
}

