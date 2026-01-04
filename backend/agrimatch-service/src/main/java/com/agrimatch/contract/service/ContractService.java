package com.agrimatch.contract.service;

import com.agrimatch.contract.dto.ContractCreateRequest;
import com.agrimatch.contract.dto.ContractQuery;
import com.agrimatch.contract.dto.ContractResponse;
import com.agrimatch.contract.dto.ContractUpdateRequest;

import java.util.List;

public interface ContractService {
    Long create(Long userId, ContractCreateRequest req);

    ContractResponse getById(Long viewerUserId, Long id);

    List<ContractResponse> list(Long viewerUserId, ContractQuery q);

    void update(Long userId, Long id, ContractUpdateRequest req);

    void delete(Long userId, Long id);

    byte[] generatePdf(Long viewerUserId, Long id);
}


