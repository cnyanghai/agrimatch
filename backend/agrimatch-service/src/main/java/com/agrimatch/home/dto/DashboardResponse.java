package com.agrimatch.home.dto;

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
    
    /** 今日被访问/咨询次数 */
    private Integer todayViewCount;
    
    /** 累计成交量（吨） */
    private Long totalDealQuantity;
    
    /** 进行中的合同数 */
    private Integer activeContractCount;
    
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
    
    public Integer getTodayViewCount() {
        return todayViewCount;
    }
    
    public void setTodayViewCount(Integer todayViewCount) {
        this.todayViewCount = todayViewCount;
    }
    
    public Long getTotalDealQuantity() {
        return totalDealQuantity;
    }
    
    public void setTotalDealQuantity(Long totalDealQuantity) {
        this.totalDealQuantity = totalDealQuantity;
    }
    
    public Integer getActiveContractCount() {
        return activeContractCount;
    }
    
    public void setActiveContractCount(Integer activeContractCount) {
        this.activeContractCount = activeContractCount;
    }
}

