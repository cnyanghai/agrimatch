package com.agrimatch.contract.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同履约节点实体
 */
public class BusContractMilestone {
    private Long id;
    private Long contractId;
    private String milestoneType;    // SHIP=发货, RECEIVE=收货, PAY=付款, INSPECT=质检, CUSTOM=自定义
    private String milestoneName;
    private String description;
    private LocalDate expectedDate;
    private LocalDate actualDate;
    private Long operatorUserId;
    private String evidenceUrl;
    private String evidenceJson;
    private String remark;
    private String status;           // pending=待完成, submitted=已提交, confirmed=已确认, rejected=已拒绝
    private Long confirmUserId;
    private LocalDateTime confirmTime;
    private Integer sortOrder;
    private String vehicleInfoJson;   // 车辆信息JSON
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public String getMilestoneType() { return milestoneType; }
    public void setMilestoneType(String milestoneType) { this.milestoneType = milestoneType; }

    public String getMilestoneName() { return milestoneName; }
    public void setMilestoneName(String milestoneName) { this.milestoneName = milestoneName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }

    public LocalDate getActualDate() { return actualDate; }
    public void setActualDate(LocalDate actualDate) { this.actualDate = actualDate; }

    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }

    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }

    public String getEvidenceJson() { return evidenceJson; }
    public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getConfirmUserId() { return confirmUserId; }
    public void setConfirmUserId(Long confirmUserId) { this.confirmUserId = confirmUserId; }

    public LocalDateTime getConfirmTime() { return confirmTime; }
    public void setConfirmTime(LocalDateTime confirmTime) { this.confirmTime = confirmTime; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getVehicleInfoJson() { return vehicleInfoJson; }
    public void setVehicleInfoJson(String vehicleInfoJson) { this.vehicleInfoJson = vehicleInfoJson; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

