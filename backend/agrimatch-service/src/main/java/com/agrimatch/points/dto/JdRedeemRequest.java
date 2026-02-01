package com.agrimatch.points.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 京东卡兑换请求
 */
public class JdRedeemRequest {

    @NotNull(message = "面额不能为空")
    private Integer faceValue; // 500 / 1000 / 2000 / 5000

    public Integer getFaceValue() { return faceValue; }
    public void setFaceValue(Integer faceValue) { this.faceValue = faceValue; }
}
