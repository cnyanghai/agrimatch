package com.agrimatch.vehicle.mapper;

import com.agrimatch.vehicle.domain.BusVehicle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VehicleMapper {
    int insert(BusVehicle vehicle);

    BusVehicle selectById(@Param("id") Long id);

    List<BusVehicle> selectByCompanyId(@Param("companyId") Long companyId);

    int update(BusVehicle vehicle);

    int logicalDelete(@Param("id") Long id);

    int clearDefault(@Param("companyId") Long companyId);

    int setDefault(@Param("id") Long id);
}

