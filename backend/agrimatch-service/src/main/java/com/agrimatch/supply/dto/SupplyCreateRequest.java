package com.agrimatch.supply.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class SupplyCreateRequest {
    @NotBlank
    @Size(max = 64)
    private String categoryName;

    /**
     * 品类ID（nht_product.id），用于前端拉取"品类参数"渲染表单；后端可选保存到 paramsJson 中。
     */
    private Long productId;

    /**
     * 供应编号/单号（可选；不传则后端生成）
     */
    @Size(max = 64)
    private String supplyNo;

    @Size(max = 128)
    private String origin;

    private BigDecimal quantity;

    /**
     * 报价类型：0=现货一口价（默认），1=基差报价
     */
    private Integer priceType = 0;

    /**
     * 出厂价（现货一口价模式必填，基差模式可为空）
     */
    private BigDecimal exFactoryPrice;

    /**
     * 基差报价明细（priceType=1 时必填）
     */
    private List<BasisQuoteRequest> basisQuotes;

    @Size(max = 255)
    private String shipAddress;

    @Size(max = 20)
    private String deliveryMode;

    @Size(max = 20)
    private String packaging;

    @Size(max = 20)
    private String storageMethod;

    private String priceRulesJson;

    private String domain;        // 所属板块
    private String tagsJson;      // 标签数据

    /**
     * 产品参数/指标选择结果 + 自定义参数（JSON字符串）
     */
    private String paramsJson;

    @Size(max = 500)
    private String remark;

    /**
     * 发布有效期（分钟），沿用需求的 expireMinutes/expireTime 机制
     */
    private Integer expireMinutes;

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

    public String getSupplyNo() {
        return supplyNo;
    }

    public void setSupplyNo(String supplyNo) {
        this.supplyNo = supplyNo;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getExFactoryPrice() {
        return exFactoryPrice;
    }

    public void setExFactoryPrice(BigDecimal exFactoryPrice) {
        this.exFactoryPrice = exFactoryPrice;
    }

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public List<BasisQuoteRequest> getBasisQuotes() {
        return basisQuotes;
    }

    public void setBasisQuotes(List<BasisQuoteRequest> basisQuotes) {
        this.basisQuotes = basisQuotes;
    }

    public String getShipAddress() {
        return shipAddress;
    }

    public void setShipAddress(String shipAddress) {
        this.shipAddress = shipAddress;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getStorageMethod() {
        return storageMethod;
    }

    public void setStorageMethod(String storageMethod) {
        this.storageMethod = storageMethod;
    }

    public String getPriceRulesJson() {
        return priceRulesJson;
    }

    public void setPriceRulesJson(String priceRulesJson) {
        this.priceRulesJson = priceRulesJson;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTagsJson() {
        return tagsJson;
    }

    public void setTagsJson(String tagsJson) {
        this.tagsJson = tagsJson;
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
}


