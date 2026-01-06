package com.agrimatch.contract.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 从报价单创建合同的请求
 */
public class ContractFromQuoteRequest {
    private Long quoteMessageId;      // 报价消息ID（必须是 ACCEPTED 状态）
    
    // 可选覆盖字段（如果不填则从报价单获取）
    private String title;             // 合同标题
    private String deliveryDate;      // 交付日期（字符串，支持多种格式）
    private String deliveryAddress;   // 交付地址
    private String paymentMethod;     // 付款方式
    private String terms;             // 合同条款

    public Long getQuoteMessageId() { return quoteMessageId; }
    public void setQuoteMessageId(Long quoteMessageId) { this.quoteMessageId = quoteMessageId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }
    
    /**
     * 解析交付日期为 LocalDate，支持多种格式
     */
    public LocalDate parseDeliveryDate() {
        if (deliveryDate == null || deliveryDate.isBlank()) return null;
        String d = deliveryDate.trim();
        
        // 尝试各种常见格式
        String[] patterns = {"yyyy-MM-dd", "yyyyMMdd", "yyyy/MM/dd", "yyyy.MM.dd"};
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(d, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {}
        }
        
        // 尝试默认 ISO 格式
        try {
            return LocalDate.parse(d);
        } catch (DateTimeParseException ignored) {}
        
        return null;
    }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }
}

