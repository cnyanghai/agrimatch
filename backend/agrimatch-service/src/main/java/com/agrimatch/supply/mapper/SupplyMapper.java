package com.agrimatch.supply.mapper;

import com.agrimatch.supply.domain.BusSupply;
import com.agrimatch.supply.dto.SupplyQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SupplyMapper {
    int insert(BusSupply s);

    BusSupply selectById(@Param("id") Long id);

    List<BusSupply> selectList(@Param("q") SupplyQuery q);

    int update(BusSupply s);

    int logicalDelete(@Param("id") Long id, @Param("userId") Long userId);

    int expireDownShelf();
}


