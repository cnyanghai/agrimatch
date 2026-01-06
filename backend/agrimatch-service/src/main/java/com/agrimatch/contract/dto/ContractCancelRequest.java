package com.agrimatch.contract.dto;

/**
 * 取消合同请求
 */
public class ContractCancelRequest {
    private String reason;  // 取消原因（可选）

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

