package com.agrimatch.futures.service.impl;

import com.agrimatch.futures.domain.FuturesContract;
import com.agrimatch.futures.dto.FuturesContractResponse;
import com.agrimatch.futures.mapper.FuturesContractMapper;
import com.agrimatch.futures.service.FuturesContractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FuturesContractServiceImpl implements FuturesContractService {

    private static final Logger log = LoggerFactory.getLogger(FuturesContractServiceImpl.class);

    @Autowired
    private FuturesContractMapper futuresContractMapper;

    // 交易所名称映射
    private static final Map<String, String> EXCHANGE_NAMES = Map.of(
            "DCE", "大商所",
            "CZCE", "郑商所",
            "SHFE", "上期所",
            "INE", "上海能源"
    );

    @Override
    public List<FuturesContractResponse> listActiveContracts(String productCode) {
        List<FuturesContract> contracts = futuresContractMapper.selectActiveContracts(productCode);
        return contracts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FuturesContractResponse getContractByCode(String contractCode) {
        FuturesContract contract = futuresContractMapper.selectByContractCode(contractCode);
        return contract != null ? toResponse(contract) : null;
    }

    @Override
    public Map<String, FuturesContractResponse> batchGetPrices(List<String> contractCodes) {
        if (contractCodes == null || contractCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<FuturesContract> contracts = futuresContractMapper.selectByContractCodes(contractCodes);
        return contracts.stream()
                .collect(Collectors.toMap(
                        FuturesContract::getContractCode,
                        this::toResponse,
                        (a, b) -> a
                ));
    }

    @Override
    public List<Map<String, String>> listProducts() {
        List<FuturesContract> products = futuresContractMapper.selectDistinctProducts();
        return products.stream()
                .map(p -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("productCode", p.getProductCode());
                    m.put("productName", p.getProductName());
                    m.put("exchangeCode", p.getExchangeCode());
                    m.put("exchangeName", EXCHANGE_NAMES.getOrDefault(p.getExchangeCode(), p.getExchangeCode()));
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void syncFuturesPrices() {
        log.info("开始同步期货价格...");
        try {
            // 1. 自动发现/确保主流合约存在（豆粕 M, 菜粕 RM）
            ensureMajorContractsExist();

            // 2. 获取所有活跃合约并同步价格
            List<FuturesContract> contracts = futuresContractMapper.selectActiveContracts(null);
            
            for (FuturesContract contract : contracts) {
                try {
                    syncSingleContractPrice(contract.getContractCode());
                } catch (Exception e) {
                    log.warn("同步合约 {} 价格失败: {}", contract.getContractCode(), e.getMessage());
                }
            }
            
            log.info("期货价格同步完成，共 {} 个合约", contracts.size());
        } catch (Exception e) {
            log.error("同步期货价格失败", e);
        }
    }

    /**
     * 确保主流品种（豆粕、菜粕、豆油、菜油）的合约存在
     * 逻辑：生成未来24个月的可能合约代码并尝试同步
     */
    private void ensureMajorContractsExist() {
        LocalDate today = LocalDate.now();
        // 豆粕 (DCE, M), 菜粕 (CZCE, RM), 豆油 (DCE, Y), 菜油 (CZCE, OI)
        Map<String, String[]> products = new LinkedHashMap<>();
        products.put("M", new String[]{"DCE", "豆粕"});
        products.put("RM", new String[]{"CZCE", "菜粕"});
        products.put("Y", new String[]{"DCE", "豆油"});
        products.put("OI", new String[]{"CZCE", "菜油"});

        for (Map.Entry<String, String[]> entry : products.entrySet()) {
            String productCode = entry.getKey();
            String exchangeCode = entry.getValue()[0];
            String productName = entry.getValue()[1];

            // 尝试未来24个月的合约，确保有足够的远期报价空间
            for (int i = 0; i < 24; i++) {
                LocalDate month = today.plusMonths(i);
                String yy = String.valueOf(month.getYear()).substring(2);
                String mm = String.format("%02d", month.getMonthValue());
                
                String contractCode;
                if ("CZCE".equals(exchangeCode)) {
                    // 郑商所格式通常为 RM505, RM601 (最后一位年份+月份)
                    contractCode = productCode + yy.substring(1) + mm;
                } else {
                    // 大商所格式通常为 M2505, M2601
                    contractCode = productCode + yy + mm;
                }

                // 检查是否已存在
                if (futuresContractMapper.selectByContractCode(contractCode) == null) {
                    // 尝试同步这个新合约，如果能获取到数据则存入数据库
                    tryFetchAndCreateContract(exchangeCode, productCode, productName, contractCode, month.withDayOfMonth(1));
                }
            }
        }
    }

    private void tryFetchAndCreateContract(String exchangeCode, String productCode, String productName, String contractCode, LocalDate deliveryMonth) {
        try {
            // 尝试两种可能的代码格式：原代码 和带 nf_ 前缀的代码
            String[] codesToTry = {contractCode, "nf_" + contractCode};
            String validResponse = null;
            
            for (String code : codesToTry) {
                String url = "http://hq.sinajs.cn/list=" + code;
                String response = fetchSinaData(url);
                if (response != null && response.contains("\"") && response.lastIndexOf("\"") > response.indexOf("\"") + 10) {
                    validResponse = response;
                    break;
                }
            }
            
            if (validResponse != null) {
                // 确实有数据，创建合约
                FuturesContract contract = new FuturesContract();
                contract.setExchangeCode(exchangeCode);
                contract.setProductCode(productCode);
                contract.setProductName(productName);
                contract.setContractCode(contractCode);
                contract.setContractName(productName + contractCode.replaceAll("[^0-9]", ""));
                contract.setDeliveryMonth(deliveryMonth);
                contract.setIsActive(1);
                contract.setSortOrder(100);
                
                futuresContractMapper.insert(contract);
                log.info("发现并创建新合约: {}", contractCode);
                
                // 立即同步一次价格
                syncSingleContractPrice(contractCode);
            }
        } catch (Exception e) {
            // 忽略错误，说明合约可能不存在
        }
    }

    private String fetchSinaData(String url) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Referer", "http://finance.sina.com.cn");
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 同步单个合约价格（使用新浪期货API）
     */
    private void syncSingleContractPrice(String contractCode) {
        try {
            // 同样尝试两种代码格式
            String[] codesToTry = {contractCode, "nf_" + contractCode};
            String response = null;
            
            for (String code : codesToTry) {
                String url = "http://hq.sinajs.cn/list=" + code;
                response = fetchSinaData(url);
                if (response != null && response.contains("\"") && response.lastIndexOf("\"") > response.indexOf("\"") + 10) {
                    break;
                }
            }
            
            if (response == null || response.isEmpty() || !response.contains("=")) {
                log.debug("合约 {} 无数据返回", contractCode);
                return;
            }
            
            // 解析响应
            String data = response.substring(response.indexOf("\"") + 1, response.lastIndexOf("\""));
            if (data.isEmpty()) {
                return;
            }
            
            String[] fields = data.split(",");
            if (fields.length < 15) {
                return;
            }
            
            // 解析价格数据 (国内期货格式)
            // 2:开盘价, 3:最高价, 4:最低价, 5:昨收价, 8:最新价, 10:昨结算, 14:成交量
            BigDecimal openPrice = parseBigDecimal(fields[2]);
            BigDecimal prevClose = parseBigDecimal(fields[5]);
            BigDecimal lastPrice = parseBigDecimal(fields[8]);
            BigDecimal highPrice = parseBigDecimal(fields[3]);
            BigDecimal lowPrice = parseBigDecimal(fields[4]);
            Long volume = parseLong(fields[14]);
            
            // 只要获取到有效价格数据（最新价或昨收价）就更新
            if ((lastPrice != null && lastPrice.compareTo(BigDecimal.ZERO) > 0) || 
                (prevClose != null && prevClose.compareTo(BigDecimal.ZERO) > 0)) {
                // 更新数据库
                futuresContractMapper.updatePrice(
                        contractCode, lastPrice, prevClose, openPrice, 
                        highPrice, lowPrice, volume, LocalDateTime.now()
                );
                log.debug("合约 {} 价格已同步: last={}, prev={}", contractCode, lastPrice, prevClose);
            }
            
        } catch (Exception e) {
            log.warn("同步合约 {} 价格失败: {}", contractCode, e.getMessage());
        }
    }

    private BigDecimal parseBigDecimal(String s) {
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private FuturesContractResponse toResponse(FuturesContract contract) {
        FuturesContractResponse resp = new FuturesContractResponse();
        resp.setId(contract.getId());
        resp.setExchangeCode(contract.getExchangeCode());
        resp.setExchangeName(EXCHANGE_NAMES.getOrDefault(contract.getExchangeCode(), contract.getExchangeCode()));
        resp.setProductCode(contract.getProductCode());
        resp.setProductName(contract.getProductName());
        resp.setContractCode(contract.getContractCode());
        resp.setContractName(contract.getContractName());
        resp.setDeliveryMonth(contract.getDeliveryMonth());
        
        // 价格降级逻辑：优先使用最新价，如果没有则使用昨收价
        BigDecimal displayPrice = contract.getLastPrice();
        if (displayPrice == null || displayPrice.compareTo(BigDecimal.ZERO) <= 0) {
            displayPrice = contract.getPrevClose();
        }
        resp.setLastPrice(displayPrice);
        
        resp.setPrevClose(contract.getPrevClose());
        resp.setOpenPrice(contract.getOpenPrice());
        resp.setHighPrice(contract.getHighPrice());
        resp.setLowPrice(contract.getLowPrice());
        resp.setVolume(contract.getVolume());
        resp.setPriceUpdateTime(contract.getPriceUpdateTime());

        // 计算涨跌额和涨跌幅
        if (resp.getLastPrice() != null && contract.getPrevClose() != null 
                && contract.getPrevClose().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = resp.getLastPrice().subtract(contract.getPrevClose());
            resp.setChangePrice(change);
            resp.setChangePercent(change.divide(contract.getPrevClose(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
        }

        // 计算距离交割天数
        if (contract.getDeliveryMonth() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), contract.getDeliveryMonth());
            resp.setDaysToDelivery((int) Math.max(0, days));
        }

        // 交易状态
        resp.setIsTrading(isTradingTime());

        return resp;
    }

    private boolean isTradingTime() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int dayOfWeek = now.getDayOfWeek().getValue();

        // 周六周日不交易
        if (dayOfWeek >= 6) return false;

        // 简化的国内期货交易时间判断
        // 日盘: 09:00-11:30, 13:30-15:00
        if (hour == 9 || hour == 10 || (hour == 11 && minute <= 30)) return true;
        if (hour == 13 && minute >= 30) return true;
        if (hour == 14) return true;
        
        // 夜盘: 21:00-23:00 (豆粕/菜粕通常到23:00)
        if (hour >= 21 && hour <= 23) return true;

        return false;
    }
}

