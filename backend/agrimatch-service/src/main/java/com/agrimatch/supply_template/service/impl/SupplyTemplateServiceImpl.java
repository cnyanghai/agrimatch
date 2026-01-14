package com.agrimatch.supply_template.service.impl;

import com.agrimatch.supply_template.domain.BusSupplyTemplate;
import com.agrimatch.supply_template.dto.SupplyTemplateCreateRequest;
import com.agrimatch.supply_template.dto.SupplyTemplateResponse;
import com.agrimatch.supply_template.mapper.SupplyTemplateMapper;
import com.agrimatch.supply_template.service.SupplyTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplyTemplateServiceImpl implements SupplyTemplateService {
    private final SupplyTemplateMapper mapper;

    public SupplyTemplateServiceImpl(SupplyTemplateMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long create(Long userId, SupplyTemplateCreateRequest request) {
        BusSupplyTemplate template = new BusSupplyTemplate();
        template.setOwnerUserId(userId);
        template.setTemplateName(request.getTemplateName());
        template.setTemplateJson(request.getTemplateJson());
        template.setIsDeleted(0);
        mapper.insert(template);
        return template.getId();
    }

    @Override
    public List<SupplyTemplateResponse> listMy(Long userId) {
        return mapper.selectByOwnerUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SupplyTemplateResponse getMyById(Long userId, Long id) {
        BusSupplyTemplate template = mapper.selectById(id);
        if (template == null || !template.getOwnerUserId().equals(userId)) {
            throw new RuntimeException("模板不存在或无权访问");
        }
        return toResponse(template);
    }

    @Override
    public void delete(Long userId, Long id) {
        int affected = mapper.softDelete(id, userId);
        if (affected == 0) {
            throw new RuntimeException("模板不存在或无权删除");
        }
    }

    private SupplyTemplateResponse toResponse(BusSupplyTemplate template) {
        SupplyTemplateResponse resp = new SupplyTemplateResponse();
        resp.setId(template.getId());
        resp.setTemplateName(template.getTemplateName());
        resp.setTemplateJson(template.getTemplateJson());
        resp.setCreateTime(template.getCreateTime());
        return resp;
    }
}

