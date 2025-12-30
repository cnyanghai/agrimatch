package com.agrimatch.requirement_template.mapper;

import com.agrimatch.requirement_template.domain.BusRequirementTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RequirementTemplateMapper {
    int insert(BusRequirementTemplate t);

    List<BusRequirementTemplate> selectByOwner(@Param("ownerUserId") Long ownerUserId);

    BusRequirementTemplate selectById(@Param("id") Long id);

    int logicalDelete(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId);
}


