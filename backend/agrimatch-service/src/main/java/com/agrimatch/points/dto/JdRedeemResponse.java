package com.agrimatch.points.dto;

/**
 * 京东卡兑换响应
 */
public class JdRedeemResponse {
    private Long redeemId;
    private String cardCode;
    private Integer faceValue;
    private Integer pointsCost;
    private Long newPointsBalance;

    public Long getRedeemId() { return redeemId; }
    public void setRedeemId(Long redeemId) { this.redeemId = redeemId; }

    public String getCardCode() { return cardCode; }
    public void setCardCode(String cardCode) { this.cardCode = cardCode; }

    public Integer getFaceValue() { return faceValue; }
    public void setFaceValue(Integer faceValue) { this.faceValue = faceValue; }

    public Integer getPointsCost() { return pointsCost; }
    public void setPointsCost(Integer pointsCost) { this.pointsCost = pointsCost; }

    public Long getNewPointsBalance() { return newPointsBalance; }
    public void setNewPointsBalance(Long newPointsBalance) { this.newPointsBalance = newPointsBalance; }
}

