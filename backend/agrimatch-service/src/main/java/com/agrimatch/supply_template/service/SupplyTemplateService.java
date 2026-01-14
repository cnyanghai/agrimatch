package com.agrimatch.supply_template.service;

import com.agrimatch.supply_template.dto.SupplyTemplateCreateRequest;
import com.agrimatch.supply_template.dto.SupplyTemplateResponse;

import java.util.List;

public interface SupplyTemplateService {
    Long create(Long userId, SupplyTemplateCreateRequest request);

    List<SupplyTemplateResponse> listMy(Long userId);

    SupplyTemplateResponse getMyById(Long userId, Long id);

    void delete(Long userId, Long id);
}

