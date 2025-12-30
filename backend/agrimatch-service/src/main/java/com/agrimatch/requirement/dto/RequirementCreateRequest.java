package com.agrimatch.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RequirementCreateRequest {
    @NotBlank
    @Size(max = 64)
    private String categoryName;

    /**
     * 品类ID（nht_product.id），用于前端拉取“品类参数”渲染表单；后端可选保存到 paramsJson 中。
     */
    private Long productId;

    /**
     * 合同编号/单号（可选；不传则后端生成）
     */
    @Size(max = 64)
    private String contractNo;

    private BigDecimal quantity;

    /**
     * 期望价格（元/吨等）
     */
    private BigDecimal expectedPrice;

    @Size(max = 20)
    private String packaging;

    @Size(max = 50)
    private String invoiceType;

    @Size(max = 20)
    private String paymentMethod;

    @Size(max = 20)
    private String deliveryMethod;

    private String paramsJson;

    @Size(max = 500)
    private String remark;

    private Integer expireMinutes;

    @Size(max = 255)
    private String purchaseAddress;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getExpectedPrice() {
        return expectedPrice;
    }

    public void setExpectedPrice(BigDecimal expectedPrice) {
        this.expectedPrice = expectedPrice;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(Integer expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    public String getPurchaseAddress() {
        return purchaseAddress;
    }

    public void setPurchaseAddress(String purchaseAddress) {
        this.purchaseAddress = purchaseAddress;
    }
}


