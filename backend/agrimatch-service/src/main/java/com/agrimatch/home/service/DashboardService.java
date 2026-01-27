package com.agrimatch.home.service;

import java.math.BigDecimal;
import com.agrimatch.follow.mapper.FollowMapper;
import com.agrimatch.home.dto.DashboardResponse;
import com.agrimatch.home.mapper.DashboardMapper;
import com.agrimatch.points.domain.BusPointsAccount;
import com.agrimatch.points.mapper.PointsMapper;
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
    private final FollowMapper followMapper;
    private final PointsMapper pointsMapper;

    public DashboardService(DashboardMapper dashboardMapper, FollowMapper followMapper, PointsMapper pointsMapper) {
        this.dashboardMapper = dashboardMapper;
        this.followMapper = followMapper;
        this.pointsMapper = pointsMapper;
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

        int activeContracts = safeInt(dashboardMapper.countActiveContracts(userId));
        response.setActiveContractCount(activeContracts);

        // 累计签署合同个数
        int totalSignedContracts = safeInt(dashboardMapper.countTotalSignedContracts(userId));
        response.setTotalSignedContractCount(totalSignedContracts);

        // 累计成交合同金额
        BigDecimal totalDealAmount = dashboardMapper.sumTotalDealAmount(userId);
        response.setTotalDealAmount(totalDealAmount != null ? totalDealAmount : BigDecimal.ZERO);

        // 关注商户数
        int followingCount = followMapper.countFollowing(userId);
        response.setFollowingCount(followingCount);

        // 用户积分余额
        BusPointsAccount account = pointsMapper.selectAccountByUserId(userId);
        long pointsBalance = (account != null && account.getPointsBalance() != null) ? account.getPointsBalance() : 0L;
        response.setPointsBalance(pointsBalance);

        log.info("[Dashboard] 用户 {} 数据: 未读={}, 待签={}, 询价={}, 里程碑={}, 发布={}, 执行中合同={}, 累计签署={}, 累计金额={}, 关注={}, 积分={}",
                userId, unreadMsg, pendingContract, pendingInquiry, pendingMilestone,
                activeListings, activeContracts, totalSignedContracts, totalDealAmount, followingCount, pointsBalance);

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

