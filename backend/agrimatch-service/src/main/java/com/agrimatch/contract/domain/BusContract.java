package com.agrimatch.contract.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同实体类 - 匹配数据库 bus_contract 表结构
 */
public class BusContract {
    private Long id;
    private String contractNo;
    
    // 关联字段
    private Long dealId;
    private Long negotiationId;
    private Long quoteId;
    private Long quoteMessageId;      // 关联报价消息ID
    private Long conversationId;       // 关联会话ID
    
    // 买卖双方公司
    private Long buyerCompanyId;
    private Long sellerCompanyId;
    
    // 产品信息
    private String productName;
    private String categoryName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    
    // 交付信息
    private String deliveryAddress;
    private LocalDate deliveryDate;
    private String paymentMethod;
    private String deliveryMode;
    
    // 条款和参数
    private String termsJson;          // JSON格式的条款
    private String paramsJson;         // 产品参数JSON
    
    // 基差报价相关
    private BigDecimal basisPrice;
    private String contractCode;
    
    // 状态 (0=草稿, 1=待签署, 2=已签署, 3=履约中, 4=已完成, 5=已取消)
    private Integer status;
    
    // 签署信息
    private LocalDateTime buyerSignTime;
    private LocalDateTime sellerSignTime;
    private String buyerSignature;
    private String sellerSignature;
    
    // PDF
    private String pdfUrl;
    private String pdfHash;
    
    private Long templateId;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }

    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public Long getNegotiationId() { return negotiationId; }
    public void setNegotiationId(Long negotiationId) { this.negotiationId = negotiationId; }

    public Long getQuoteId() { return quoteId; }
    public void setQuoteId(Long quoteId) { this.quoteId = quoteId; }

    public Long getQuoteMessageId() { return quoteMessageId; }
    public void setQuoteMessageId(Long quoteMessageId) { this.quoteMessageId = quoteMessageId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getBuyerCompanyId() { return buyerCompanyId; }
    public void setBuyerCompanyId(Long buyerCompanyId) { this.buyerCompanyId = buyerCompanyId; }

    public Long getSellerCompanyId() { return sellerCompanyId; }
    public void setSellerCompanyId(Long sellerCompanyId) { this.sellerCompanyId = sellerCompanyId; }

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

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }

    public String getTermsJson() { return termsJson; }
    public void setTermsJson(String termsJson) { this.termsJson = termsJson; }

    public String getParamsJson() { return paramsJson; }
    public void setParamsJson(String paramsJson) { this.paramsJson = paramsJson; }

    public BigDecimal getBasisPrice() { return basisPrice; }
    public void setBasisPrice(BigDecimal basisPrice) { this.basisPrice = basisPrice; }

    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getBuyerSignTime() { return buyerSignTime; }
    public void setBuyerSignTime(LocalDateTime buyerSignTime) { this.buyerSignTime = buyerSignTime; }

    public LocalDateTime getSellerSignTime() { return sellerSignTime; }
    public void setSellerSignTime(LocalDateTime sellerSignTime) { this.sellerSignTime = sellerSignTime; }

    public String getBuyerSignature() { return buyerSignature; }
    public void setBuyerSignature(String buyerSignature) { this.buyerSignature = buyerSignature; }

    public String getSellerSignature() { return sellerSignature; }
    public void setSellerSignature(String sellerSignature) { this.sellerSignature = sellerSignature; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getPdfHash() { return pdfHash; }
    public void setPdfHash(String pdfHash) { this.pdfHash = pdfHash; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
