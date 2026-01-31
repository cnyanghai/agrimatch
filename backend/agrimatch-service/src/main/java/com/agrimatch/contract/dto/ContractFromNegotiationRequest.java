package com.agrimatch.contract.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 从议价会话创建合同的请求（无需报价单）
 */
public class ContractFromNegotiationRequest {

    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    // 产品信息
    private String productName;
    private String categoryName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;

    // 基差信息（可选）
    private BigDecimal basisPrice;
    private String contractCode;
    private String priceType;         // SPOT or BASIS

    // 交付信息
    private String deliveryDate;
    private String deliveryAddress;
    private String deliveryMode;
    private String paymentMethod;

    // 扩展字段
    private String paramsJson;

    // ---------- getters & setters ----------

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getBasisPrice() { return basisPrice; }
    public void setBasisPrice(BigDecimal basisPrice) { this.basisPrice = basisPrice; }

    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }

    public String getPriceType() { return priceType; }
    public void setPriceType(String priceType) { this.priceType = priceType; }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

    /**
     * 解析交付日期为 LocalDate
     */
    public LocalDate parseDeliveryDate() {
        if (deliveryDate == null || deliveryDate.isBlank()) return null;
        String d = deliveryDate.trim();
        String[] patterns = {"yyyy-MM-dd", "yyyyMMdd", "yyyy/MM/dd", "yyyy.MM.dd"};
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(d, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {}
        }
        try { return LocalDate.parse(d); } catch (DateTimeParseException ignored) {}
        return null;
    }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getParamsJson() { return paramsJson; }
    public void setParamsJson(String paramsJson) { this.paramsJson = paramsJson; }
}
