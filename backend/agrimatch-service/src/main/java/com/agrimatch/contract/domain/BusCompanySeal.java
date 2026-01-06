package com.agrimatch.contract.domain;

import java.time.LocalDateTime;

/**
 * 公司电子章实体
 */
public class BusCompanySeal {
    private Long id;
    private Long companyId;
    private Long userId;
    private String sealType;      // official=公章, contract=合同章, finance=财务章
    private String sealName;
    private String sealUrl;
    private Boolean isGenerated;  // 是否系统生成
    private Boolean isDefault;    // 是否默认章
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSealType() { return sealType; }
    public void setSealType(String sealType) { this.sealType = sealType; }

    public String getSealName() { return sealName; }
    public void setSealName(String sealName) { this.sealName = sealName; }

    public String getSealUrl() { return sealUrl; }
    public void setSealUrl(String sealUrl) { this.sealUrl = sealUrl; }

    public Boolean getIsGenerated() { return isGenerated; }
    public void setIsGenerated(Boolean isGenerated) { this.isGenerated = isGenerated; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

