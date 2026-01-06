package com.agrimatch.contract.dto;

import java.time.LocalDate;

/**
 * 创建履约节点请求
 */
public class MilestoneCreateRequest {
    private String milestoneType;    // SHIP=发货, RECEIVE=收货, PAY=付款, INSPECT=质检, CUSTOM=自定义
    private String milestoneName;    // 节点名称
    private String description;      // 节点描述
    private LocalDate expectedDate;  // 预期完成日期
    private Integer sortOrder;       // 排序序号

    public String getMilestoneType() { return milestoneType; }
    public void setMilestoneType(String milestoneType) { this.milestoneType = milestoneType; }

    public String getMilestoneName() { return milestoneName; }
    public void setMilestoneName(String milestoneName) { this.milestoneName = milestoneName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}

