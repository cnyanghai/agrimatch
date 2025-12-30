package com.agrimatch.requirement_template.service;

import com.agrimatch.requirement_template.dto.RequirementTemplateCreateRequest;
import com.agrimatch.requirement_template.dto.RequirementTemplateResponse;

import java.util.List;

public interface RequirementTemplateService {
    Long create(Long ownerUserId, RequirementTemplateCreateRequest req);

    List<RequirementTemplateResponse> listMy(Long ownerUserId);

    RequirementTemplateResponse getMyById(Long ownerUserId, Long id);

    void delete(Long ownerUserId, Long id);
}


