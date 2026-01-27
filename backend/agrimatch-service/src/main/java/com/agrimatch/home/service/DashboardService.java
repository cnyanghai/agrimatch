package com.agrimatch.home.service;

import com.agrimatch.home.dto.DashboardResponse;
import com.agrimatch.home.mapper.DashboardMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 控制台首页服务
 * 聚合待办事项和业务统计数据
 */
@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private final DashboardMapper dashboardMapper;

    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    /**
     * 获取用户的控制台首页数据
     * 注意：系统不再区分供应商/采购商，用户可同时发布供应和采购
     * @param userId 用户ID
     * @return 首页数据
     */
    public DashboardResponse getDashboardData(Long userId) {
        log.info("[Dashboard] 开始获取用户 {} 的首页数据", userId);
        DashboardResponse response = new DashboardResponse();

        // 待办事项
        int unreadMsg = safeInt(dashboardMapper.countUnreadMessages(userId));
        int pendingContract = safeInt(dashboardMapper.countPendingContracts(userId));
        int pendingInquiry = safeInt(dashboardMapper.countPendingInquiries(userId));
        int pendingMilestone = safeInt(dashboardMapper.countPendingMilestones(userId));
        response.setUnreadMessageCount(unreadMsg);
        response.setPendingContractCount(pendingContract);
        response.setPendingInquiryCount(pendingInquiry);
        response.setPendingMilestoneCount(pendingMilestone);

        // 业务统计 - 统计用户发布的所有活跃供应和采购
        int activeSupplies = safeInt(dashboardMapper.countActiveSupplies(userId));
        int activeRequirements = safeInt(dashboardMapper.countActiveRequirements(userId));
        int activeListings = activeSupplies + activeRequirements;
        response.setMyActiveListingCount(activeListings);

        int todayViews = safeInt(dashboardMapper.countTodayViews(userId));
        long totalDeal = safeLong(dashboardMapper.sumTotalDealQuantity(userId));
        int activeContracts = safeInt(dashboardMapper.countActiveContracts(userId));
        response.setTodayViewCount(todayViews);
        response.setTotalDealQuantity(totalDeal);
        response.setActiveContractCount(activeContracts);

        log.info("[Dashboard] 用户 {} 数据: 未读={}, 待签={}, 询价={}, 里程碑={}, 供应={}, 采购={}, 发布={}, 咨询={}, 成交={}, 合同={}",
                userId, unreadMsg, pendingContract, pendingInquiry, pendingMilestone,
                activeSupplies, activeRequirements, activeListings, todayViews, totalDeal, activeContracts);

        return response;
    }
    
    /**
     * 获取待办事项总数（用于显示角标）
     */
    public int getTotalPendingCount(Long userId) {
        int total = 0;
        total += safeInt(dashboardMapper.countUnreadMessages(userId));
        total += safeInt(dashboardMapper.countPendingContracts(userId));
        total += safeInt(dashboardMapper.countPendingInquiries(userId));
        total += safeInt(dashboardMapper.countPendingMilestones(userId));
        return total;
    }
    
    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }
    
    private long safeLong(Long value) {
        return value != null ? value : 0L;
    }
}

