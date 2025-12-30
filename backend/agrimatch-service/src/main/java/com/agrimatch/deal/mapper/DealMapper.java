package com.agrimatch.deal.mapper;

import com.agrimatch.deal.domain.BusDeal;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DealMapper {
    int insert(BusDeal d);

    BusDeal selectById(@Param("id") Long id);

    List<BusDeal> selectByRequirementId(@Param("requirementId") Long requirementId);

    BigDecimal sumConfirmedQuantityByRequirementId(@Param("requirementId") Long requirementId);
}


