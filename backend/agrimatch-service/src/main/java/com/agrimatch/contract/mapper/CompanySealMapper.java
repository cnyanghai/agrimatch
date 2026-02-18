package com.agrimatch.contract.mapper;

import com.agrimatch.contract.domain.BusCompanySeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompanySealMapper {
    int insert(BusCompanySeal seal);

    BusCompanySeal selectById(@Param("id") Long id);

    List<BusCompanySeal> selectByCompanyId(@Param("companyId") Long companyId);

    List<BusCompanySeal> selectByUserId(@Param("userId") Long userId);

    BusCompanySeal selectDefaultByCompanyId(@Param("companyId") Long companyId);

    BusCompanySeal selectDefaultByUserId(@Param("userId") Long userId);

    int update(BusCompanySeal seal);

    int clearDefault(@Param("companyId") Long companyId);

    int logicalDelete(@Param("id") Long id);
}

