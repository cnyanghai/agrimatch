package com.agrimatch.points.dto;

/**
 * 创建充值订单响应
 */
public class RechargeCreateResponse {
    private String orderNo;
    private String qrCodeUrl;
    private Integer amount;
    private Integer points;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}

