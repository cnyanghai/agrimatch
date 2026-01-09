package com.agrimatch.futures.service;

import com.agrimatch.futures.dto.FuturesContractResponse;

import java.util.List;
import java.util.Map;

public interface FuturesContractService {

    /**
     * 获取活跃合约列表
     * @param productCode 品种代码（可选，如 M/RM）
     */
    List<FuturesContractResponse> listActiveContracts(String productCode);

    /**
     * 获取单个合约详情（含价格）
     */
    FuturesContractResponse getContractByCode(String contractCode);

    /**
     * 批量获取合约价格
     */
    Map<String, FuturesContractResponse> batchGetPrices(List<String> contractCodes);

    /**
     * 获取产品品种列表
     */
    List<Map<String, String>> listProducts();

    /**
     * 同步期货价格（从第三方API）
     */
    void syncFuturesPrices();
}

