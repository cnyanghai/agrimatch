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
     * 会话表字段：a_user_id, b_user_id
     * 消息表字段：from_user_id, to_user_id
     */
    @Select("SELECT COUNT(*) FROM bus_chat_message m " +
            "JOIN bus_chat_conversation c ON m.conversation_id = c.id " +
            "WHERE ((c.a_user_id = #{userId} AND m.from_user_id != #{userId}) " +
            "    OR (c.b_user_id = #{userId} AND m.from_user_id != #{userId})) " +
            "AND m.is_read = 0 AND m.is_deleted = 0")
    Integer countUnreadMessages(@Param("userId") Long userId);
    
    /**
     * 统计待签署合同数（用户是买方或卖方，且对应方未签署）
     * 通过用户的company_id关联合同
     * 用签署时间是否为NULL判断是否已签署
     */
    @Select("SELECT COUNT(*) FROM bus_contract c, sys_user u " +
            "WHERE u.user_id = #{userId} " +
            "AND c.status = 1 " +
            "AND ((c.buyer_company_id = u.company_id AND c.buyer_sign_time IS NULL) " +
            "  OR (c.seller_company_id = u.company_id AND c.seller_sign_time IS NULL)) " +
            "AND c.is_deleted = 0")
    Integer countPendingContracts(@Param("userId") Long userId);
    
    /**
     * 统计待回复询价数（收到的QUOTE消息且未回复）
     * 会话表字段：a_user_id, b_user_id
     * 消息表字段：from_user_id, to_user_id
     */
    @Select("SELECT COUNT(*) FROM bus_chat_message m " +
            "JOIN bus_chat_conversation c ON m.conversation_id = c.id " +
            "WHERE ((c.a_user_id = #{userId} AND m.from_user_id = c.b_user_id) " +
            "    OR (c.b_user_id = #{userId} AND m.from_user_id = c.a_user_id)) " +
            "AND m.msg_type = 'QUOTE' " +
            "AND m.is_deleted = 0 " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM bus_chat_message reply " +
            "  WHERE reply.conversation_id = m.conversation_id " +
            "  AND reply.from_user_id = #{userId} " +
            "  AND reply.create_time > m.create_time" +
            ")")
    Integer countPendingInquiries(@Param("userId") Long userId);
    
    /**
     * 统计待确认里程碑数（用户是买方或卖方，且里程碑状态为已提交待确认）
     * 通过用户的company_id关联合同
     * milestone.status: pending=待完成, submitted=已提交待确认
     */
    @Select("SELECT COUNT(*) FROM bus_contract_milestone m, bus_contract c, sys_user u " +
            "WHERE m.contract_id = c.id " +
            "AND u.user_id = #{userId} " +
            "AND m.status = 'submitted' " +
            "AND (c.buyer_company_id = u.company_id OR c.seller_company_id = u.company_id) " +
            "AND m.is_deleted = 0 AND c.is_deleted = 0")
    Integer countPendingMilestones(@Param("userId") Long userId);
    
    /**
     * 统计用户发布的活跃供应数
     * status: 0=发布中, 1=部分成交 都算活跃
     */
    @Select("SELECT COUNT(*) FROM bus_supply " +
            "WHERE user_id = #{userId} " +
            "AND status IN (0, 1) " +
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
     * 会话表字段：a_user_id, b_user_id
     */
    @Select("SELECT COUNT(*) FROM bus_chat_conversation " +
            "WHERE ((a_user_id = #{userId} AND b_user_id != #{userId}) " +
            "    OR (b_user_id = #{userId} AND a_user_id != #{userId})) " +
            "AND DATE(create_time) = CURDATE() " +
            "AND is_deleted = 0")
    Integer countTodayViews(@Param("userId") Long userId);
    
    /**
     * 统计用户累计成交量（吨）- 已完成合同的数量之和
     * 通过用户的company_id关联合同
     * contract.status: 4=已完成
     */
    @Select("SELECT COALESCE(SUM(c.quantity), 0) FROM bus_contract c, sys_user u " +
            "WHERE u.user_id = #{userId} " +
            "AND (c.buyer_company_id = u.company_id OR c.seller_company_id = u.company_id) " +
            "AND c.status = 4 " +
            "AND c.is_deleted = 0")
    Long sumTotalDealQuantity(@Param("userId") Long userId);
    
    /**
     * 统计进行中的合同数（已签署、履约中）
     * 通过用户的company_id关联合同
     * contract.status: 2=已签署, 3=履约中
     */
    @Select("SELECT COUNT(*) FROM bus_contract c, sys_user u " +
            "WHERE u.user_id = #{userId} " +
            "AND (c.buyer_company_id = u.company_id OR c.seller_company_id = u.company_id) " +
            "AND c.status IN (2, 3) " +
            "AND c.is_deleted = 0")
    Integer countActiveContracts(@Param("userId") Long userId);
}

