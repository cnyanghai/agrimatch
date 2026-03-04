package com.agrimatch.points.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建充值订单请求
 */
public class RechargeCreateRequest {

    @NotNull(message = "金额不能为空")
    @Min(value = 1, message = "最低充值1元")
    @Max(value = 10000, message = "单次最多充值10000元")
    private Integer amount;

    @NotBlank(message = "支付渠道不能为空")
    private String payChannel; // wechat / alipay

    private String payType; // NATIVE / H5 / JSAPI

    private String clientIp; // H5 必需

    private String openid; // JSAPI 必需

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getPayChannel() { return payChannel; }
    public void setPayChannel(String payChannel) { this.payChannel = payChannel; }

    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
}
