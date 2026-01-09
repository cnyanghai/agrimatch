package com.agrimatch.futures.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.futures.dto.FuturesContractResponse;
import com.agrimatch.futures.service.FuturesContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 期货合约接口
 */
@RestController
@RequestMapping("/api/futures")
public class FuturesContractController {

    @Autowired
    private FuturesContractService futuresContractService;

    /**
     * 获取活跃合约列表
     * @param productCode 品种代码（可选，如 M 豆粕、RM 菜粕）
     */
    @GetMapping("/contracts")
    public Result<List<FuturesContractResponse>> listContracts(
            @RequestParam(name = "productCode", required = false) String productCode) {
        List<FuturesContractResponse> list = futuresContractService.listActiveContracts(productCode);
        return Result.success(list);
    }

    /**
     * 获取单个合约详情
     */
    @GetMapping("/contracts/{contractCode}")
    public Result<FuturesContractResponse> getContract(@PathVariable("contractCode") String contractCode) {
        FuturesContractResponse contract = futuresContractService.getContractByCode(contractCode);
        if (contract == null) {
            return Result.fail(404, "合约不存在");
        }
        return Result.success(contract);
    }

    /**
     * 批量获取合约价格
     */
    @PostMapping("/prices/batch")
    public Result<Map<String, FuturesContractResponse>> batchGetPrices(@RequestBody List<String> contractCodes) {
        Map<String, FuturesContractResponse> prices = futuresContractService.batchGetPrices(contractCodes);
        return Result.success(prices);
    }

    /**
     * 获取产品品种列表
     */
    @GetMapping("/products")
    public Result<List<Map<String, String>>> listProducts() {
        return Result.success(futuresContractService.listProducts());
    }

    /**
     * 手动触发价格同步（管理员用）
     */
    @PostMapping("/sync")
    public Result<String> syncPrices() {
        futuresContractService.syncFuturesPrices();
        return Result.success("价格同步任务已触发，请稍后刷新查看最新价格");
    }
}
