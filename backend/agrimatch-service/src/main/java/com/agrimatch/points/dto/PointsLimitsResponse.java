package com.agrimatch.points.dto;

/**
 * 积分限额信息响应
 */
public class PointsLimitsResponse {
    private Integer rechargeMax = 10000;         // 单次充值上限
    private Integer rechargeDayMax = 50000;      // 日充值上限
    private Integer redeemMax = 5000;            // 单次兑换上限
    private Integer redeemDayMax = 10000;        // 日兑换上限
    private Integer todayRechargeTotal = 0;      // 今日已充值
    private Integer todayRedeemTotal = 0;        // 今日已兑换

    public Integer getRechargeMax() { return rechargeMax; }
    public void setRechargeMax(Integer rechargeMax) { this.rechargeMax = rechargeMax; }

    public Integer getRechargeDayMax() { return rechargeDayMax; }
    public void setRechargeDayMax(Integer rechargeDayMax) { this.rechargeDayMax = rechargeDayMax; }

    public Integer getRedeemMax() { return redeemMax; }
    public void setRedeemMax(Integer redeemMax) { this.redeemMax = redeemMax; }

    public Integer getRedeemDayMax() { return redeemDayMax; }
    public void setRedeemDayMax(Integer redeemDayMax) { this.redeemDayMax = redeemDayMax; }

    public Integer getTodayRechargeTotal() { return todayRechargeTotal; }
    public void setTodayRechargeTotal(Integer todayRechargeTotal) { this.todayRechargeTotal = todayRechargeTotal; }

    public Integer getTodayRedeemTotal() { return todayRedeemTotal; }
    public void setTodayRedeemTotal(Integer todayRedeemTotal) { this.todayRedeemTotal = todayRedeemTotal; }
}

