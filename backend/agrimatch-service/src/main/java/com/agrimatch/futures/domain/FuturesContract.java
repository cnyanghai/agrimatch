package com.agrimatch.futures.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 期货合约实体
 */
public class FuturesContract {
    private Long id;
    private String exchangeCode;      // 交易所代码 (DCE/CZCE/SHFE)
    private String productCode;       // 品种代码 (M豆粕/RM菜粕)
    private String productName;       // 品种名称
    private String contractCode;      // 合约代码 (M2505)
    private String contractName;      // 合约名称 (豆粕2505)
    private LocalDate deliveryMonth;  // 交割月份
    private BigDecimal lastPrice;     // 最新价格
    private BigDecimal prevClose;     // 昨收价
    private BigDecimal openPrice;     // 开盘价
    private BigDecimal highPrice;     // 最高价
    private BigDecimal lowPrice;      // 最低价
    private Long volume;              // 成交量
    private LocalDateTime priceUpdateTime; // 价格更新时间
    private Integer isActive;         // 是否活跃
    private Integer sortOrder;        // 排序
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExchangeCode() {
        return exchangeCode;
    }

    public void setExchangeCode(String exchangeCode) {
        this.exchangeCode = exchangeCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public LocalDate getDeliveryMonth() {
        return deliveryMonth;
    }

    public void setDeliveryMonth(LocalDate deliveryMonth) {
        this.deliveryMonth = deliveryMonth;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public BigDecimal getPrevClose() {
        return prevClose;
    }

    public void setPrevClose(BigDecimal prevClose) {
        this.prevClose = prevClose;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public LocalDateTime getPriceUpdateTime() {
        return priceUpdateTime;
    }

    public void setPriceUpdateTime(LocalDateTime priceUpdateTime) {
        this.priceUpdateTime = priceUpdateTime;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

