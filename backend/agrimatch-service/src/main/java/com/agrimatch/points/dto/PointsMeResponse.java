package com.agrimatch.points.dto;

import java.math.BigDecimal;

public class PointsMeResponse {
    private Long pointsBalance;
    private BigDecimal cnyBalance;

    public Long getPointsBalance() {
        return pointsBalance;
    }

    public void setPointsBalance(Long pointsBalance) {
        this.pointsBalance = pointsBalance;
    }

    public BigDecimal getCnyBalance() {
        return cnyBalance;
    }

    public void setCnyBalance(BigDecimal cnyBalance) {
        this.cnyBalance = cnyBalance;
    }
}


