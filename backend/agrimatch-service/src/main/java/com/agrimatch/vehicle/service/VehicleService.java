package com.agrimatch.vehicle.service;

import com.agrimatch.vehicle.dto.VehicleCreateRequest;
import com.agrimatch.vehicle.dto.VehicleResponse;

import java.util.List;

public interface VehicleService {
    Long create(Long userId, VehicleCreateRequest req);

    VehicleResponse getById(Long id);

    List<VehicleResponse> listByCompany(Long userId);

    void update(Long userId, Long vehicleId, VehicleCreateRequest req);

    void delete(Long userId, Long vehicleId);

    void setDefault(Long userId, Long vehicleId);
}

