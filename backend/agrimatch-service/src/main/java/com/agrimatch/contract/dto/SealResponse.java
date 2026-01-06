package com.agrimatch.contract.dto;

import java.time.LocalDateTime;

/**
 * 电子章响应
 */
public class SealResponse {
    private Long id;
    private Long companyId;
    private String sealName;
    private String sealType;
    private String sealUrl;
    private Boolean isGenerated;
    private Boolean isDefault;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getSealName() { return sealName; }
    public void setSealName(String sealName) { this.sealName = sealName; }

    public String getSealType() { return sealType; }
    public void setSealType(String sealType) { this.sealType = sealType; }

    public String getSealUrl() { return sealUrl; }
    public void setSealUrl(String sealUrl) { this.sealUrl = sealUrl; }

    public Boolean getIsGenerated() { return isGenerated; }
    public void setIsGenerated(Boolean isGenerated) { this.isGenerated = isGenerated; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

