package com.agrimatch.contract.service;

import com.agrimatch.contract.dto.*;

import java.util.List;

public interface ContractService {
    Long create(Long userId, ContractCreateRequest req);
    
    /**
     * 从已确认的报价单创建合同草稿
     */
    Long createFromQuote(Long userId, ContractFromQuoteRequest req);

    ContractResponse getById(Long viewerUserId, Long id);

    List<ContractResponse> list(Long viewerUserId, ContractQuery q);

    void update(Long userId, Long id, ContractUpdateRequest req);

    void delete(Long userId, Long id);
    
    /**
     * 发送合同给对方签署（状态从 draft -> pending）
     */
    void sendForSigning(Long userId, Long contractId);
    
    /**
     * 签署合同
     */
    void sign(Long userId, Long contractId, ContractSignRequest req);
    
    /**
     * 检查合同是否双方都已签署，如果是则更新状态为 signed
     */
    void checkAndUpdateSignStatus(Long contractId);

    byte[] generatePdf(Long viewerUserId, Long id);
}


