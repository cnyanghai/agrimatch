package com.agrimatch.points.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GiftRequest {
    @NotNull(message = "接收者不能为空")
    private Long receiverUserId;

    @NotNull(message = "积分数不能为空")
    @Min(value = 1, message = "最少赠送1积分")
    @Max(value = 5000, message = "单次最多赠送5000积分")
    private Integer points;

    private String message;

    public Long getReceiverUserId() { return receiverUserId; }
    public void setReceiverUserId(Long receiverUserId) { this.receiverUserId = receiverUserId; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
