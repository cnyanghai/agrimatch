package com.agrimatch.requirement.service;

import com.agrimatch.requirement.dto.RequirementCreateRequest;
import com.agrimatch.requirement.dto.RequirementQuery;
import com.agrimatch.requirement.dto.RequirementResponse;
import com.agrimatch.requirement.dto.RequirementUpdateRequest;

import java.util.List;

public interface RequirementService {
    Long create(Long userId, RequirementCreateRequest req);

    RequirementResponse getById(Long id);

    /**
     * 列表：用于按当前用户公司坐标计算 distance（Beta），并返回剩余数量（支持多次成交）
     */
    List<RequirementResponse> list(Long viewerUserId, RequirementQuery query);

    void update(Long userId, Long id, RequirementUpdateRequest req);

    void delete(Long userId, Long id);
}


