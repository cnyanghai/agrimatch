package com.agrimatch.requirement_template.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.requirement_template.domain.BusRequirementTemplate;
import com.agrimatch.requirement_template.dto.RequirementTemplateCreateRequest;
import com.agrimatch.requirement_template.dto.RequirementTemplateResponse;
import com.agrimatch.requirement_template.mapper.RequirementTemplateMapper;
import com.agrimatch.requirement_template.service.RequirementTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class RequirementTemplateServiceImpl implements RequirementTemplateService {
    private final RequirementTemplateMapper mapper;

    public RequirementTemplateServiceImpl(RequirementTemplateMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long create(Long ownerUserId, RequirementTemplateCreateRequest req) {
        if (ownerUserId == null) throw new ApiException(401, "未登录");
        if (req == null || !StringUtils.hasText(req.getTemplateName()) || !StringUtils.hasText(req.getTemplateJson())) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        BusRequirementTemplate t = new BusRequirementTemplate();
        t.setOwnerUserId(ownerUserId);
        t.setTemplateName(req.getTemplateName().trim());
        t.setTemplateJson(req.getTemplateJson());
        int rows = mapper.insert(t);
        if (rows != 1 || t.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);
        return t.getId();
    }

    @Override
    public List<RequirementTemplateResponse> listMy(Long ownerUserId) {
        if (ownerUserId == null) throw new ApiException(401, "未登录");
        List<BusRequirementTemplate> list = mapper.selectByOwner(ownerUserId);
        List<RequirementTemplateResponse> out = new ArrayList<>();
        for (BusRequirementTemplate t : list) {
            out.add(toResponse(t));
        }
        return out;
    }

    @Override
    public RequirementTemplateResponse getMyById(Long ownerUserId, Long id) {
        if (ownerUserId == null) throw new ApiException(401, "未登录");
        BusRequirementTemplate t = mapper.selectById(id);
        if (t == null || !ownerUserId.equals(t.getOwnerUserId())) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        return toResponse(t);
    }

    @Override
    public void delete(Long ownerUserId, Long id) {
        if (ownerUserId == null) throw new ApiException(401, "未登录");
        int rows = mapper.logicalDelete(id, ownerUserId);
        if (rows != 1) throw new ApiException(ResultCode.NOT_FOUND);
    }

    private static RequirementTemplateResponse toResponse(BusRequirementTemplate t) {
        RequirementTemplateResponse r = new RequirementTemplateResponse();
        r.setId(t.getId());
        r.setTemplateName(t.getTemplateName());
        r.setTemplateJson(t.getTemplateJson());
        r.setCreateTime(t.getCreateTime());
        r.setUpdateTime(t.getUpdateTime());
        return r;
    }
}


