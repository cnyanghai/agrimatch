package com.agrimatch.home.dto;

import java.math.BigDecimal;

/**
 * 控制台首页数据响应
 * 包含待办事项统计和业务数据统计
 */
public class DashboardResponse {

    // ========== 待办事项 ==========

    /** 未读消息数 */
    private Integer unreadMessageCount;

    /** 待签署合同数 */
    private Integer pendingContractCount;

    /** 待回复询价数（收到的询价/报价消息） */
    private Integer pendingInquiryCount;

    /** 待确认里程碑数 */
    private Integer pendingMilestoneCount;

    // ========== 业务统计 ==========

    /** 我发布的供应/采购数（活跃中的） */
    private Integer myActiveListingCount;

    /** 进行中的合同数 */
    private Integer activeContractCount;

    /** 累计签署合同个数（status >= 2） */
    private Integer totalSignedContractCount;

    /** 累计成交合同金额（status = 4 的合同总金额） */
    private BigDecimal totalDealAmount;

    // ========== 用户相关 ==========

    /** 关注商户数 */
    private Integer followingCount;

    /** 用户积分余额 */
    private Long pointsBalance;
    
    // ========== Getters & Setters ==========
    
    public Integer getUnreadMessageCount() {
        return unreadMessageCount;
    }
    
    public void setUnreadMessageCount(Integer unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }
    
    public Integer getPendingContractCount() {
        return pendingContractCount;
    }
    
    public void setPendingContractCount(Integer pendingContractCount) {
        this.pendingContractCount = pendingContractCount;
    }
    
    public Integer getPendingInquiryCount() {
        return pendingInquiryCount;
    }
    
    public void setPendingInquiryCount(Integer pendingInquiryCount) {
        this.pendingInquiryCount = pendingInquiryCount;
    }
    
    public Integer getPendingMilestoneCount() {
        return pendingMilestoneCount;
    }
    
    public void setPendingMilestoneCount(Integer pendingMilestoneCount) {
        this.pendingMilestoneCount = pendingMilestoneCount;
    }
    
    public Integer getMyActiveListingCount() {
        return myActiveListingCount;
    }
    
    public void setMyActiveListingCount(Integer myActiveListingCount) {
        this.myActiveListingCount = myActiveListingCount;
    }
    
    public Integer getActiveContractCount() {
        return activeContractCount;
    }

    public void setActiveContractCount(Integer activeContractCount) {
        this.activeContractCount = activeContractCount;
    }

    public Integer getTotalSignedContractCount() {
        return totalSignedContractCount;
    }

    public void setTotalSignedContractCount(Integer totalSignedContractCount) {
        this.totalSignedContractCount = totalSignedContractCount;
    }

    public BigDecimal getTotalDealAmount() {
        return totalDealAmount;
    }

    public void setTotalDealAmount(BigDecimal totalDealAmount) {
        this.totalDealAmount = totalDealAmount;
    }

    public Integer getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
    }

    public Long getPointsBalance() {
        return pointsBalance;
    }

    public void setPointsBalance(Long pointsBalance) {
        this.pointsBalance = pointsBalance;
    }
}

