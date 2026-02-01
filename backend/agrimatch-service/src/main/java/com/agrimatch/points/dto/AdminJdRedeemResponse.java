package com.agrimatch.points.dto;

import java.time.LocalDateTime;

/**
 * 管理端京东卡兑换列表
 */
public class AdminJdRedeemResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String nickName;
    private Integer pointsCost;
    private Integer faceValue;
    private String cardCode;
    private Integer status;
    private Long adminUserId;
    private String adminRemark;
    private LocalDateTime createTime;
    private LocalDateTime fulfillTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public Integer getPointsCost() { return pointsCost; }
    public void setPointsCost(Integer pointsCost) { this.pointsCost = pointsCost; }

    public Integer getFaceValue() { return faceValue; }
    public void setFaceValue(Integer faceValue) { this.faceValue = faceValue; }

    public String getCardCode() { return cardCode; }
    public void setCardCode(String cardCode) { this.cardCode = cardCode; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getAdminUserId() { return adminUserId; }
    public void setAdminUserId(Long adminUserId) { this.adminUserId = adminUserId; }

    public String getAdminRemark() { return adminRemark; }
    public void setAdminRemark(String adminRemark) { this.adminRemark = adminRemark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getFulfillTime() { return fulfillTime; }
    public void setFulfillTime(LocalDateTime fulfillTime) { this.fulfillTime = fulfillTime; }
}
