package com.agrimatch.points.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BusPointsAccount {
    private Long id;
    private Long userId;
    private Long pointsBalance;
    private BigDecimal cnyBalance;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}


