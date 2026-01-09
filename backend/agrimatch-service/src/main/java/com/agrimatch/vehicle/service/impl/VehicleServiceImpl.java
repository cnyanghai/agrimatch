package com.agrimatch.vehicle.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import com.agrimatch.vehicle.domain.BusVehicle;
import com.agrimatch.vehicle.dto.VehicleCreateRequest;
import com.agrimatch.vehicle.dto.VehicleResponse;
import com.agrimatch.vehicle.mapper.VehicleMapper;
import com.agrimatch.vehicle.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {
    private final VehicleMapper vehicleMapper;
    private final UserMapper userMapper;

    public VehicleServiceImpl(VehicleMapper vehicleMapper, UserMapper userMapper) {
        this.vehicleMapper = vehicleMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public Long create(Long userId, VehicleCreateRequest req) {
        Long companyId = getCompanyId(userId);

        BusVehicle vehicle = new BusVehicle();
        vehicle.setCompanyId(companyId);
        vehicle.setDriverName(req.getDriverName().trim());
        vehicle.setDriverIdCard(req.getDriverIdCard().trim());
        vehicle.setPlateNumber(req.getPlateNumber().trim().toUpperCase());
        vehicle.setDriverPhone(req.getDriverPhone().trim());
        vehicle.setVehicleType(req.getVehicleType() != null ? req.getVehicleType().trim() : null);
        vehicle.setRemark(req.getRemark() != null ? req.getRemark().trim() : null);
        vehicle.setIsDefault(0);

        int rows = vehicleMapper.insert(vehicle);
        if (rows != 1 || vehicle.getId() == null) {
            throw new ApiException(ResultCode.SERVER_ERROR);
        }
        return vehicle.getId();
    }

    @Override
    public VehicleResponse getById(Long id) {
        BusVehicle vehicle = vehicleMapper.selectById(id);
        if (vehicle == null) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        return toResponse(vehicle);
    }

    @Override
    public List<VehicleResponse> listByCompany(Long userId) {
        Long companyId = getCompanyId(userId);
        List<BusVehicle> vehicles = vehicleMapper.selectByCompanyId(companyId);
        List<VehicleResponse> out = new ArrayList<>();
        for (BusVehicle v : vehicles) {
            out.add(toResponse(v));
        }
        return out;
    }

    @Override
    @Transactional
    public void update(Long userId, Long vehicleId, VehicleCreateRequest req) {
        Long companyId = getCompanyId(userId);

        BusVehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        if (!companyId.equals(vehicle.getCompanyId())) {
            throw new ApiException(403, "无权修改此车辆信息");
        }

        vehicle.setDriverName(req.getDriverName().trim());
        vehicle.setDriverIdCard(req.getDriverIdCard().trim());
        vehicle.setPlateNumber(req.getPlateNumber().trim().toUpperCase());
        vehicle.setDriverPhone(req.getDriverPhone().trim());
        vehicle.setVehicleType(req.getVehicleType() != null ? req.getVehicleType().trim() : null);
        vehicle.setRemark(req.getRemark() != null ? req.getRemark().trim() : null);

        int rows = vehicleMapper.update(vehicle);
        if (rows != 1) {
            throw new ApiException(ResultCode.SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void delete(Long userId, Long vehicleId) {
        Long companyId = getCompanyId(userId);

        BusVehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        if (!companyId.equals(vehicle.getCompanyId())) {
            throw new ApiException(403, "无权删除此车辆信息");
        }

        int rows = vehicleMapper.logicalDelete(vehicleId);
        if (rows != 1) {
            throw new ApiException(ResultCode.SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long vehicleId) {
        Long companyId = getCompanyId(userId);

        BusVehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        if (!companyId.equals(vehicle.getCompanyId())) {
            throw new ApiException(403, "无权操作此车辆");
        }

        // 清除该公司所有车辆的默认状态
        vehicleMapper.clearDefault(companyId);
        // 设置当前车辆为默认
        vehicleMapper.setDefault(vehicleId);
    }

    private Long getCompanyId(Long userId) {
        if (userId == null) {
            throw new ApiException(401, "未登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ApiException(401, "用户不存在");
        }
        if (user.getCompanyId() == null) {
            throw new ApiException(400, "请先完善公司信息");
        }
        return user.getCompanyId();
    }

    private static VehicleResponse toResponse(BusVehicle v) {
        VehicleResponse r = new VehicleResponse();
        r.setId(v.getId());
        r.setCompanyId(v.getCompanyId());
        r.setDriverName(v.getDriverName());
        r.setDriverIdCard(v.getDriverIdCard());
        r.setPlateNumber(v.getPlateNumber());
        r.setDriverPhone(v.getDriverPhone());
        r.setVehicleType(v.getVehicleType());
        r.setIsDefault(v.getIsDefault() != null && v.getIsDefault() == 1);
        r.setRemark(v.getRemark());
        r.setCreateTime(v.getCreateTime());
        return r;
    }
}

