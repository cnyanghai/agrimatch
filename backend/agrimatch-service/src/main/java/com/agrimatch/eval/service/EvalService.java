package com.agrimatch.eval.service;

import com.agrimatch.eval.dto.EvalCreateRequest;
import com.agrimatch.eval.dto.EvalResponse;

import java.util.List;

public interface EvalService {
    Long create(Long fromUserId, EvalCreateRequest req);

    List<EvalResponse> listByCompany(Long toCompanyId);
}


