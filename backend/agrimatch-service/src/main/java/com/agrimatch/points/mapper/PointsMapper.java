package com.agrimatch.points.mapper;

import com.agrimatch.points.domain.BusPointsAccount;
import com.agrimatch.points.domain.BusPointsTx;
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
}


