package com.agrimatch.points.dto;

import java.time.LocalDateTime;

/**
 * 用户兑换记录详情
 */
public class JdRedeemDetailResponse {
    private Long id;
    private Integer pointsCost;
    private Integer faceValue;
    private String cardCode;
    private Integer status; // 0待发卡 1已发卡 2已失败
    private String adminRemark;
    private LocalDateTime createTime;
    private LocalDateTime fulfillTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPointsCost() { return pointsCost; }
    public void setPointsCost(Integer pointsCost) { this.pointsCost = pointsCost; }

    public Integer getFaceValue() { return faceValue; }
    public void setFaceValue(Integer faceValue) { this.faceValue = faceValue; }

    public String getCardCode() { return cardCode; }
    public void setCardCode(String cardCode) { this.cardCode = cardCode; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getAdminRemark() { return adminRemark; }
    public void setAdminRemark(String adminRemark) { this.adminRemark = adminRemark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getFulfillTime() { return fulfillTime; }
    public void setFulfillTime(LocalDateTime fulfillTime) { this.fulfillTime = fulfillTime; }
}
