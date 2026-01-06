package com.agrimatch.contract.domain;

import java.time.LocalDateTime;

/**
 * 合同签署记录实体
 */
public class BusContractSignature {
    private Long id;
    private Long contractId;
    private Long userId;
    private Long companyId;
    private String partyType;      // party_a / party_b
    private String signType;       // seal=盖章, handwrite=手写签名, typed=打字签名, seal_handwrite=盖章+签名
    private Long sealId;
    private String sealUrl;
    private String signatureUrl;
    private String signerName;
    private String signerTitle;
    private String signIp;
    private LocalDateTime signTime;
    private Integer isDeleted;
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getPartyType() { return partyType; }
    public void setPartyType(String partyType) { this.partyType = partyType; }

    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }

    public Long getSealId() { return sealId; }
    public void setSealId(Long sealId) { this.sealId = sealId; }

    public String getSealUrl() { return sealUrl; }
    public void setSealUrl(String sealUrl) { this.sealUrl = sealUrl; }

    public String getSignatureUrl() { return signatureUrl; }
    public void setSignatureUrl(String signatureUrl) { this.signatureUrl = signatureUrl; }

    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }

    public String getSignerTitle() { return signerTitle; }
    public void setSignerTitle(String signerTitle) { this.signerTitle = signerTitle; }

    public String getSignIp() { return signIp; }
    public void setSignIp(String signIp) { this.signIp = signIp; }

    public LocalDateTime getSignTime() { return signTime; }
    public void setSignTime(LocalDateTime signTime) { this.signTime = signTime; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

