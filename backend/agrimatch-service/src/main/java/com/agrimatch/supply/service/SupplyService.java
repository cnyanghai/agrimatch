package com.agrimatch.supply.service;

import com.agrimatch.supply.dto.SupplyCreateRequest;
import com.agrimatch.supply.dto.SupplyQuery;
import com.agrimatch.supply.dto.SupplyResponse;
import com.agrimatch.supply.dto.SupplyUpdateRequest;

import java.util.List;

public interface SupplyService {
    Long create(Long userId, SupplyCreateRequest req);

    SupplyResponse getById(Long id);

    /**
     * 列表：用于按当前用户公司坐标计算 distance/delivered_price（Beta）
     */
    List<SupplyResponse> list(Long viewerUserId, SupplyQuery query);

    void update(Long userId, Long id, SupplyUpdateRequest req);

    void delete(Long userId, Long id);
}


