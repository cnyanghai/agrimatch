package com.agrimatch.supply_template.mapper;

import com.agrimatch.supply_template.domain.BusSupplyTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SupplyTemplateMapper {
    int insert(BusSupplyTemplate template);

    List<BusSupplyTemplate> selectByOwnerUserId(@Param("ownerUserId") Long ownerUserId);

    BusSupplyTemplate selectById(@Param("id") Long id);

    int softDelete(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId);
}

