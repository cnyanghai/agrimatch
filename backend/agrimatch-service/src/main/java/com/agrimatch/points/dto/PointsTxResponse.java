package com.agrimatch.points.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PointsTxResponse {
    private Long id;
    private String txType;
    private Long pointsDelta;
    private BigDecimal cnyDelta;
    private String remark;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}


