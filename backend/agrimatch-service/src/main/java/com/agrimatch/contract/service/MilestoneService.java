package com.agrimatch.contract.service;

import com.agrimatch.contract.dto.MilestoneCreateRequest;
import com.agrimatch.contract.dto.MilestoneResponse;
import com.agrimatch.contract.dto.MilestoneSubmitRequest;

import java.util.List;

/**
 * 合同履约节点服务
 */
public interface MilestoneService {
    
    /**
     * 添加履约节点
     */
    Long create(Long userId, Long contractId, MilestoneCreateRequest req);
    
    /**
     * 获取合同的所有履约节点
     */
    List<MilestoneResponse> listByContract(Long userId, Long contractId);
    
    /**
     * 提交履约节点（上传凭证）
     */
    void submit(Long userId, Long milestoneId, MilestoneSubmitRequest req);
    
    /**
     * 确认履约节点
     */
    void confirm(Long userId, Long milestoneId);
    
    /**
     * 拒绝履约节点
     */
    void reject(Long userId, Long milestoneId, String reason);
    
    /**
     * 删除履约节点（仅未提交的可删除）
     */
    void delete(Long userId, Long milestoneId);
    
    /**
     * 检查合同是否所有节点都已完成
     */
    boolean isAllCompleted(Long contractId);
}

