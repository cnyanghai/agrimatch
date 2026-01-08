package com.agrimatch.points.service;

import com.agrimatch.points.dto.PointsMeResponse;
import com.agrimatch.points.dto.PointsTxResponse;

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
}


