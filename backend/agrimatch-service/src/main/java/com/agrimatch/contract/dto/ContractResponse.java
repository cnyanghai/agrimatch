package com.agrimatch.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ContractResponse {
    private Long id;
    private Long quoteMessageId;
    private Long conversationId;
    private String contractNo;

    // 买卖双方基本信息
    private Long buyerCompanyId;
    private Long sellerCompanyId;
    private String buyerCompanyName;
    private String sellerCompanyName;
    
    // 买方详细信息
    private String buyerLicenseNo;       // 营业执照号
    private String buyerContacts;        // 联系人
    private String buyerPhone;           // 电话
    private String buyerAddress;         // 详细地址
    private String buyerBankInfo;        // 银行信息 JSON
    
    // 卖方详细信息
    private String sellerLicenseNo;
    private String sellerContacts;
    private String sellerPhone;
    private String sellerAddress;
    private String sellerBankInfo;

    // 产品信息
    private String productName;
    private String categoryName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private String paramsJson;
    private BigDecimal totalAmount;
    
    // 产品参数（解析后的结构化数据）
    private List<Map<String, Object>> productParams;

    // 交付信息
    private LocalDate deliveryDate;
    private String deliveryAddress;
    private String paymentMethod;
    private String deliveryMode;
    private String termsJson;
    
    // 格式化合同条款
    private String formattedTerms;

    // 状态 (0=草稿, 1=待签署, 2=已签署, 3=履约中, 4=已完成, 5=已取消)
    private Integer status;
    
    // 签署信息
    private LocalDateTime buyerSignTime;
    private LocalDateTime sellerSignTime;
    private Boolean buyerSigned;
    private Boolean sellerSigned;
    
    private String pdfHash;
    private String pdfUrl;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuoteMessageId() { return quoteMessageId; }
    public void setQuoteMessageId(Long quoteMessageId) { this.quoteMessageId = quoteMessageId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }

    public Long getBuyerCompanyId() { return buyerCompanyId; }
    public void setBuyerCompanyId(Long buyerCompanyId) { this.buyerCompanyId = buyerCompanyId; }

    public Long getSellerCompanyId() { return sellerCompanyId; }
    public void setSellerCompanyId(Long sellerCompanyId) { this.sellerCompanyId = sellerCompanyId; }

    public String getBuyerCompanyName() { return buyerCompanyName; }
    public void setBuyerCompanyName(String buyerCompanyName) { this.buyerCompanyName = buyerCompanyName; }

    public String getSellerCompanyName() { return sellerCompanyName; }
    public void setSellerCompanyName(String sellerCompanyName) { this.sellerCompanyName = sellerCompanyName; }

    // 买方详细信息
    public String getBuyerLicenseNo() { return buyerLicenseNo; }
    public void setBuyerLicenseNo(String buyerLicenseNo) { this.buyerLicenseNo = buyerLicenseNo; }

    public String getBuyerContacts() { return buyerContacts; }
    public void setBuyerContacts(String buyerContacts) { this.buyerContacts = buyerContacts; }

    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }

    public String getBuyerAddress() { return buyerAddress; }
    public void setBuyerAddress(String buyerAddress) { this.buyerAddress = buyerAddress; }

    public String getBuyerBankInfo() { return buyerBankInfo; }
    public void setBuyerBankInfo(String buyerBankInfo) { this.buyerBankInfo = buyerBankInfo; }

    // 卖方详细信息
    public String getSellerLicenseNo() { return sellerLicenseNo; }
    public void setSellerLicenseNo(String sellerLicenseNo) { this.sellerLicenseNo = sellerLicenseNo; }

    public String getSellerContacts() { return sellerContacts; }
    public void setSellerContacts(String sellerContacts) { this.sellerContacts = sellerContacts; }

    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }

    public String getSellerAddress() { return sellerAddress; }
    public void setSellerAddress(String sellerAddress) { this.sellerAddress = sellerAddress; }

    public String getSellerBankInfo() { return sellerBankInfo; }
    public void setSellerBankInfo(String sellerBankInfo) { this.sellerBankInfo = sellerBankInfo; }

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

    public String getParamsJson() { return paramsJson; }
    public void setParamsJson(String paramsJson) { this.paramsJson = paramsJson; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }

    public String getTermsJson() { return termsJson; }
    public void setTermsJson(String termsJson) { this.termsJson = termsJson; }

    public List<Map<String, Object>> getProductParams() { return productParams; }
    public void setProductParams(List<Map<String, Object>> productParams) { this.productParams = productParams; }

    public String getFormattedTerms() { return formattedTerms; }
    public void setFormattedTerms(String formattedTerms) { this.formattedTerms = formattedTerms; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getBuyerSignTime() { return buyerSignTime; }
    public void setBuyerSignTime(LocalDateTime buyerSignTime) { this.buyerSignTime = buyerSignTime; }

    public LocalDateTime getSellerSignTime() { return sellerSignTime; }
    public void setSellerSignTime(LocalDateTime sellerSignTime) { this.sellerSignTime = sellerSignTime; }

    public Boolean getBuyerSigned() { return buyerSigned; }
    public void setBuyerSigned(Boolean buyerSigned) { this.buyerSigned = buyerSigned; }

    public Boolean getSellerSigned() { return sellerSigned; }
    public void setSellerSigned(Boolean sellerSigned) { this.sellerSigned = sellerSigned; }

    public String getPdfHash() { return pdfHash; }
    public void setPdfHash(String pdfHash) { this.pdfHash = pdfHash; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
