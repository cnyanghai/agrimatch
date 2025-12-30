package com.agrimatch.requirement.mapper;

import com.agrimatch.requirement.domain.BusRequirement;
import com.agrimatch.requirement.dto.RequirementQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RequirementMapper {
    int insert(BusRequirement req);

    BusRequirement selectById(@Param("id") Long id);

    List<BusRequirement> selectList(@Param("q") RequirementQuery query);

    int update(BusRequirement req);

    int logicalDelete(@Param("id") Long id, @Param("userId") Long userId);
}


