package com.agrimatch.points.mapper;

import com.agrimatch.points.domain.BusJdRedeem;
import com.agrimatch.points.domain.BusPointsAccount;
import com.agrimatch.points.domain.BusPointsTx;
import com.agrimatch.points.domain.BusRechargeOrder;
import com.agrimatch.points.dto.AdminJdRedeemResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PointsMapper {
    BusPointsAccount selectAccountByUserId(@Param("userId") Long userId);

    BusPointsAccount selectAccountByUserIdForUpdate(@Param("userId") Long userId);

    int insertAccount(BusPointsAccount a);

    int updateAccountBalance(@Param("userId") Long userId,
                             @Param("pointsBalance") Long pointsBalance,
                             @Param("cnyBalance") java.math.BigDecimal cnyBalance);

    int insertTx(BusPointsTx tx);

    List<BusPointsTx> selectTxListByUserId(@Param("userId") Long userId);

    // 充值订单相关
    int insertRechargeOrder(BusRechargeOrder order);

    BusRechargeOrder selectRechargeOrderByOrderNo(@Param("orderNo") String orderNo);

    int updateRechargeOrderStatus(@Param("orderNo") String orderNo,
                                   @Param("status") Integer status,
                                   @Param("tradeNo") String tradeNo);

    Integer sumTodayRechargeByUserId(@Param("userId") Long userId);

    // 京东卡兑换相关
    int insertJdRedeem(BusJdRedeem redeem);

    Integer sumTodayJdRedeemByUserId(@Param("userId") Long userId);

    List<BusJdRedeem> selectJdRedeemListByUserId(@Param("userId") Long userId);

    // 管理端：查询全部兑换订单（含用户信息）
    List<AdminJdRedeemResponse> selectAllJdRedeems(@Param("status") Integer status);

    // 按ID查询兑换记录
    BusJdRedeem selectJdRedeemById(@Param("id") Long id);

    // 管理员发卡
    int fulfillJdRedeem(@Param("id") Long id,
                        @Param("cardCode") String cardCode,
                        @Param("adminUserId") Long adminUserId);

    // 管理员拒绝
    int failJdRedeem(@Param("id") Long id,
                     @Param("adminUserId") Long adminUserId,
                     @Param("adminRemark") String adminRemark);
}


