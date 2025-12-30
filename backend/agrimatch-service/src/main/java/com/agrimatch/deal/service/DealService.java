package com.agrimatch.deal.service;

import com.agrimatch.deal.dto.DealCreateRequest;
import com.agrimatch.deal.dto.DealResponse;

import java.util.List;

public interface DealService {
    Long create(Long buyerUserId, DealCreateRequest req);

    DealResponse getById(Long id);

    List<DealResponse> listByRequirement(Long requirementId);
}


