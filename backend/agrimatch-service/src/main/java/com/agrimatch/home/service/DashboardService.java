package com.agrimatch.home.service;

import com.agrimatch.home.dto.DashboardResponse;
import com.agrimatch.home.mapper.DashboardMapper;
import org.springframework.stereotype.Service;

/**
 * 控制台首页服务
 * 聚合待办事项和业务统计数据
 */
@Service
public class DashboardService {
    
    private final DashboardMapper dashboardMapper;
    
    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }
    
    /**
     * 获取用户的控制台首页数据
     * @param userId 用户ID
     * @param isBuyer 是否采购商
     * @param isSeller 是否供应商
     * @return 首页数据
     */
    public DashboardResponse getDashboardData(Long userId, boolean isBuyer, boolean isSeller) {
        DashboardResponse response = new DashboardResponse();
        
        // 待办事项
        response.setUnreadMessageCount(safeInt(dashboardMapper.countUnreadMessages(userId)));
        response.setPendingContractCount(safeInt(dashboardMapper.countPendingContracts(userId)));
        response.setPendingInquiryCount(safeInt(dashboardMapper.countPendingInquiries(userId)));
        response.setPendingMilestoneCount(safeInt(dashboardMapper.countPendingMilestones(userId)));
        
        // 业务统计
        int activeListings = 0;
        if (isSeller) {
            activeListings += safeInt(dashboardMapper.countActiveSupplies(userId));
        }
        if (isBuyer) {
            activeListings += safeInt(dashboardMapper.countActiveRequirements(userId));
        }
        response.setMyActiveListingCount(activeListings);
        
        response.setTodayViewCount(safeInt(dashboardMapper.countTodayViews(userId)));
        response.setTotalDealQuantity(safeLong(dashboardMapper.sumTotalDealQuantity(userId)));
        response.setActiveContractCount(safeInt(dashboardMapper.countActiveContracts(userId)));
        
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

