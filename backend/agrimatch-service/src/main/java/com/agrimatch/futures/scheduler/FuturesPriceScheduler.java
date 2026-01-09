package com.agrimatch.futures.scheduler;

import com.agrimatch.futures.service.FuturesContractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 期货价格定时同步任务
 * 
 * 使用新浪期货API同步大商所、郑商所的期货实时价格
 * 交易时间：
 * - 日盘：09:00-10:15, 10:30-11:30, 13:30-15:00
 * - 夜盘：21:00-23:30（部分品种）
 */
@Component
public class FuturesPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(FuturesPriceScheduler.class);

    @Autowired
    private FuturesContractService futuresContractService;

    /**
     * 每5分钟同步一次期货价格
     * 仅在交易时间执行（简化逻辑，全天执行）
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 30 * 1000)
    public void syncPrices() {
        try {
            log.debug("开始定时同步期货价格...");
            futuresContractService.syncFuturesPrices();
        } catch (Exception e) {
            log.error("定时同步期货价格失败", e);
        }
    }
}

