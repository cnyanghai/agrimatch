package com.agrimatch.home.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 控制台首页数据Mapper
 */
@Mapper
public interface DashboardMapper {
    
    /**
     * 统计用户未读消息数
     */
    @Select("SELECT COUNT(*) FROM bus_chat_message m " +
            "JOIN bus_chat_conversation c ON m.conversation_id = c.id " +
            "WHERE ((c.user1_id = #{userId} AND m.sender_id != #{userId}) " +
            "    OR (c.user2_id = #{userId} AND m.sender_id != #{userId})) " +
            "AND m.is_read = 0 AND m.is_deleted = 0")
    Integer countUnreadMessages(@Param("userId") Long userId);
    
    /**
     * 统计待签署合同数（用户是买方或卖方，且对应方未签署）
     */
    @Select("SELECT COUNT(*) FROM bus_contract " +
            "WHERE status = 1 " +
            "AND ((buyer_user_id = #{userId} AND buyer_signed = 0) " +
            "  OR (seller_user_id = #{userId} AND seller_signed = 0)) " +
            "AND is_deleted = 0")
    Integer countPendingContracts(@Param("userId") Long userId);
    
    /**
     * 统计待回复询价数（收到的QUOTE消息且未回复）
     */
    @Select("SELECT COUNT(*) FROM bus_chat_message m " +
            "JOIN bus_chat_conversation c ON m.conversation_id = c.id " +
            "WHERE ((c.user1_id = #{userId} AND m.sender_id = c.user2_id) " +
            "    OR (c.user2_id = #{userId} AND m.sender_id = c.user1_id)) " +
            "AND m.msg_type = 'QUOTE' " +
            "AND m.is_deleted = 0 " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM bus_chat_message reply " +
            "  WHERE reply.conversation_id = m.conversation_id " +
            "  AND reply.sender_id = #{userId} " +
            "  AND reply.create_time > m.create_time" +
            ")")
    Integer countPendingInquiries(@Param("userId") Long userId);
    
    /**
     * 统计待确认里程碑数（用户是买方或卖方，且里程碑状态为待确认）
     */
    @Select("SELECT COUNT(*) FROM bus_contract_milestone m " +
            "JOIN bus_contract c ON m.contract_id = c.id " +
            "WHERE m.status = 1 " +
            "AND (c.buyer_user_id = #{userId} OR c.seller_user_id = #{userId}) " +
            "AND m.is_deleted = 0 AND c.is_deleted = 0")
    Integer countPendingMilestones(@Param("userId") Long userId);
    
    /**
     * 统计用户发布的活跃供应数
     */
    @Select("SELECT COUNT(*) FROM bus_supply " +
            "WHERE user_id = #{userId} " +
            "AND status = 1 " +
            "AND is_deleted = 0 " +
            "AND (expire_time IS NULL OR expire_time > NOW())")
    Integer countActiveSupplies(@Param("userId") Long userId);
    
    /**
     * 统计用户发布的活跃采购数
     */
    @Select("SELECT COUNT(*) FROM bus_requirement " +
            "WHERE user_id = #{userId} " +
            "AND status = 0 " +
            "AND is_deleted = 0")
    Integer countActiveRequirements(@Param("userId") Long userId);
    
    /**
     * 统计今日被查看/咨询次数（暂用会话创建数代替）
     */
    @Select("SELECT COUNT(*) FROM bus_chat_conversation " +
            "WHERE ((user1_id = #{userId} AND user2_id != #{userId}) " +
            "    OR (user2_id = #{userId} AND user1_id != #{userId})) " +
            "AND DATE(create_time) = CURDATE() " +
            "AND is_deleted = 0")
    Integer countTodayViews(@Param("userId") Long userId);
    
    /**
     * 统计用户累计成交量（吨）- 已完成合同的数量之和
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM bus_contract " +
            "WHERE (buyer_user_id = #{userId} OR seller_user_id = #{userId}) " +
            "AND status = 4 " +
            "AND is_deleted = 0")
    Long sumTotalDealQuantity(@Param("userId") Long userId);
    
    /**
     * 统计进行中的合同数（已签署、履约中）
     */
    @Select("SELECT COUNT(*) FROM bus_contract " +
            "WHERE (buyer_user_id = #{userId} OR seller_user_id = #{userId}) " +
            "AND status IN (2, 3) " +
            "AND is_deleted = 0")
    Integer countActiveContracts(@Param("userId") Long userId);
}

