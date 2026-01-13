package com.agrimatch.points.service;

import com.agrimatch.points.dto.*;

import java.util.List;

public interface PointsService {
    PointsMeResponse me(Long userId);

    PointsMeResponse recharge(Long userId, Long points);

    PointsMeResponse redeem(Long userId, Long points);

    List<PointsTxResponse> myTx(Long userId);

    /**
     * 获取用户当前积分余额
     */
    Long getBalance(Long userId);

    /**
     * 扣除积分（用于赏金求助发布）
     */
    void deduct(Long userId, Long points, String remark);

    /**
     * 增加积分（用于赏金采纳发放）
     */
    void add(Long userId, Long points, String remark);

    /**
     * 创建充值订单
     */
    RechargeCreateResponse createRechargeOrder(Long userId, RechargeCreateRequest req);

    /**
     * 查询充值订单状态
     */
    Integer getRechargeOrderStatus(String orderNo);

    /**
     * 支付回调确认（模拟）
     */
    void confirmRechargeOrder(String orderNo, String tradeNo);

    /**
     * 兑换京东卡
     */
    JdRedeemResponse redeemJdCard(Long userId, JdRedeemRequest req);

    /**
     * 获取限额信息
     */
    PointsLimitsResponse getLimits(Long userId);
}


