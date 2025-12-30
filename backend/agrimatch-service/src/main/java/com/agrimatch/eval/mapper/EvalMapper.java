package com.agrimatch.eval.mapper;

import com.agrimatch.eval.domain.BusOrderEval;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EvalMapper {
    int insert(BusOrderEval e);

    List<BusOrderEval> selectByCompany(@Param("toCompanyId") Long toCompanyId);

    int countByDealAndFromUser(@Param("dealId") Long dealId, @Param("fromUserId") Long fromUserId);
}


