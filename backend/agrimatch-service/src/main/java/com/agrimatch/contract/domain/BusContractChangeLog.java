package com.agrimatch.contract.domain;

import java.time.LocalDateTime;

/**
 * 合同变更审计日志实体
 */
public class BusContractChangeLog {
    private Long id;
    private Long contractId;
    private String changeType;       // CREATE=创建, UPDATE=修改, STATUS=状态变更, SIGN=签署, MILESTONE=履约节点
    private String changeDesc;
    private String beforeJson;
    private String afterJson;
    private Long operatorUserId;
    private String operatorIp;
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public String getChangeDesc() { return changeDesc; }
    public void setChangeDesc(String changeDesc) { this.changeDesc = changeDesc; }

    public String getBeforeJson() { return beforeJson; }
    public void setBeforeJson(String beforeJson) { this.beforeJson = beforeJson; }

    public String getAfterJson() { return afterJson; }
    public void setAfterJson(String afterJson) { this.afterJson = afterJson; }

    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }

    public String getOperatorIp() { return operatorIp; }
    public void setOperatorIp(String operatorIp) { this.operatorIp = operatorIp; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

