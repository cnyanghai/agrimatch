package com.agrimatch.futures.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 期货合约响应DTO
 */
public class FuturesContractResponse {
    private Long id;
    private String exchangeCode;
    private String exchangeName;      // 交易所名称
    private String productCode;
    private String productName;
    private String contractCode;
    private String contractName;
    private LocalDate deliveryMonth;
    private BigDecimal lastPrice;
    private BigDecimal prevClose;
    private BigDecimal changePrice;   // 涨跌额
    private BigDecimal changePercent; // 涨跌幅
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Long volume;
    private LocalDateTime priceUpdateTime;
    private Integer daysToDelivery;   // 距离交割天数
    private Boolean isTrading;        // 是否在交易时间内

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

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
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

    public BigDecimal getChangePrice() {
        return changePrice;
    }

    public void setChangePrice(BigDecimal changePrice) {
        this.changePrice = changePrice;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
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

    public Integer getDaysToDelivery() {
        return daysToDelivery;
    }

    public void setDaysToDelivery(Integer daysToDelivery) {
        this.daysToDelivery = daysToDelivery;
    }

    public Boolean getIsTrading() {
        return isTrading;
    }

    public void setIsTrading(Boolean trading) {
        isTrading = trading;
    }
}

