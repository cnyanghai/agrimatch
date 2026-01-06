package com.agrimatch.contract.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 提交履约节点请求
 */
public class MilestoneSubmitRequest {
    private LocalDate actualDate;         // 实际完成日期
    private String evidenceUrl;           // 凭证URL（单个）
    private List<String> evidenceUrls;    // 凭证URL列表（多个）
    private String remark;                // 备注

    public LocalDate getActualDate() { return actualDate; }
    public void setActualDate(LocalDate actualDate) { this.actualDate = actualDate; }

    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }

    public List<String> getEvidenceUrls() { return evidenceUrls; }
    public void setEvidenceUrls(List<String> evidenceUrls) { this.evidenceUrls = evidenceUrls; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

