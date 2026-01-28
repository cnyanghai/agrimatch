package com.agrimatch.contract.dto;

/**
 * 公司合同统计响应
 */
public class ContractStatsResponse {

    /** 累计签订合同数 */
    private Long signedContractCount;

    /** 合作商户数 */
    private Long partnerCount;

    public ContractStatsResponse() {}

    public ContractStatsResponse(Long signedContractCount, Long partnerCount) {
        this.signedContractCount = signedContractCount;
        this.partnerCount = partnerCount;
    }

    public Long getSignedContractCount() {
        return signedContractCount;
    }

    public void setSignedContractCount(Long signedContractCount) {
        this.signedContractCount = signedContractCount;
    }

    public Long getPartnerCount() {
        return partnerCount;
    }

    public void setPartnerCount(Long partnerCount) {
        this.partnerCount = partnerCount;
    }
}
