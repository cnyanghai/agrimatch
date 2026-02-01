package com.agrimatch.admin.dto;

import java.math.BigDecimal;

public class AdminPointsOverviewResponse {
    private BigDecimal totalRechargeAmount;
    private BigDecimal todayRechargeAmount;
    private BigDecimal totalCardAmount;
    private BigDecimal todayCardAmount;
    private Long totalGiftPoints;
    private Long totalCirculatingPoints;

    public BigDecimal getTotalRechargeAmount() { return totalRechargeAmount; }
    public void setTotalRechargeAmount(BigDecimal totalRechargeAmount) { this.totalRechargeAmount = totalRechargeAmount; }

    public BigDecimal getTodayRechargeAmount() { return todayRechargeAmount; }
    public void setTodayRechargeAmount(BigDecimal todayRechargeAmount) { this.todayRechargeAmount = todayRechargeAmount; }

    public BigDecimal getTotalCardAmount() { return totalCardAmount; }
    public void setTotalCardAmount(BigDecimal totalCardAmount) { this.totalCardAmount = totalCardAmount; }

    public BigDecimal getTodayCardAmount() { return todayCardAmount; }
    public void setTodayCardAmount(BigDecimal todayCardAmount) { this.todayCardAmount = todayCardAmount; }

    public Long getTotalGiftPoints() { return totalGiftPoints; }
    public void setTotalGiftPoints(Long totalGiftPoints) { this.totalGiftPoints = totalGiftPoints; }

    public Long getTotalCirculatingPoints() { return totalCirculatingPoints; }
    public void setTotalCirculatingPoints(Long totalCirculatingPoints) { this.totalCirculatingPoints = totalCirculatingPoints; }
}
