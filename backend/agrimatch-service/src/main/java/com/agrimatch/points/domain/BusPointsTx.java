package com.agrimatch.points.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BusPointsTx {
    private Long id;
    private Long userId;
    private String txType;
    private Long pointsDelta;
    private BigDecimal cnyDelta;
    private String remark;
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

    public String getTxType() {
        return txType;
    }

    public void setTxType(String txType) {
        this.txType = txType;
    }

    public Long getPointsDelta() {
        return pointsDelta;
    }

    public void setPointsDelta(Long pointsDelta) {
        this.pointsDelta = pointsDelta;
    }

    public BigDecimal getCnyDelta() {
        return cnyDelta;
    }

    public void setCnyDelta(BigDecimal cnyDelta) {
        this.cnyDelta = cnyDelta;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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


