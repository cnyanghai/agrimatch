package com.agrimatch.points.dto;

import java.util.Map;

/**
 * 创建充值订单响应
 */
public class RechargeCreateResponse {
    private String orderNo;
    private Integer amount;      // 元
    private Integer points;
    private String payType;      // NATIVE / H5 / JSAPI
    private String codeUrl;      // Native: 微信支付URL（前端转QR码）
    private String h5Url;        // H5: 跳转链接
    private Map<String, String> jsapiParams; // JSAPI: 拉起支付参数

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }

    public String getCodeUrl() { return codeUrl; }
    public void setCodeUrl(String codeUrl) { this.codeUrl = codeUrl; }

    public String getH5Url() { return h5Url; }
    public void setH5Url(String h5Url) { this.h5Url = h5Url; }

    public Map<String, String> getJsapiParams() { return jsapiParams; }
    public void setJsapiParams(Map<String, String> jsapiParams) { this.jsapiParams = jsapiParams; }
}
