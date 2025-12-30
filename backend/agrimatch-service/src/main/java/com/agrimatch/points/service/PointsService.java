package com.agrimatch.points.service;

import com.agrimatch.points.dto.PointsMeResponse;
import com.agrimatch.points.dto.PointsTxResponse;

import java.util.List;

public interface PointsService {
    PointsMeResponse me(Long userId);

    PointsMeResponse recharge(Long userId, Long points);

    PointsMeResponse redeem(Long userId, Long points);

    List<PointsTxResponse> myTx(Long userId);
}


