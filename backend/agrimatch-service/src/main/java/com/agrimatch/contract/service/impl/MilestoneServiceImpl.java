package com.agrimatch.contract.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.contract.domain.BusContract;
import com.agrimatch.contract.domain.BusContractMilestone;
import com.agrimatch.contract.dto.MilestoneCreateRequest;
import com.agrimatch.contract.dto.MilestoneResponse;
import com.agrimatch.contract.dto.MilestoneSubmitRequest;
import com.agrimatch.contract.mapper.ContractMapper;
import com.agrimatch.contract.mapper.ContractMilestoneMapper;
import com.agrimatch.contract.service.MilestoneService;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MilestoneServiceImpl implements MilestoneService {

    private final ContractMilestoneMapper milestoneMapper;
    private final ContractMapper contractMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public MilestoneServiceImpl(ContractMilestoneMapper milestoneMapper,
                                ContractMapper contractMapper,
                                UserMapper userMapper,
                                ObjectMapper objectMapper) {
        this.milestoneMapper = milestoneMapper;
        this.contractMapper = contractMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Long create(Long userId, Long contractId, MilestoneCreateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new ApiException(401, "未登录");
        
        BusContract contract = contractMapper.selectById(contractId);
        if (contract == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "合同不存在");
        
        // 检查权限：只有合同双方可以添加节点
        Long userCompanyId = user.getCompanyId();
        if (userCompanyId == null || 
            (!userCompanyId.equals(contract.getBuyerCompanyId()) && !userCompanyId.equals(contract.getSellerCompanyId()))) {
            throw new ApiException(403, "无权操作此合同");
        }
        
        // 检查状态：只有状态 2(已签署) 或 3(履约中) 可以添加节点
        Integer status = contract.getStatus();
        if (status == null || (status != 2 && status != 3)) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "合同未生效或已完成");
        }
        
        BusContractMilestone milestone = new BusContractMilestone();
        milestone.setContractId(contractId);
        milestone.setMilestoneType(req.getMilestoneType() != null ? req.getMilestoneType() : "CUSTOM");
        milestone.setMilestoneName(req.getMilestoneName());
        milestone.setDescription(req.getDescription());
        milestone.setExpectedDate(req.getExpectedDate());
        milestone.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        milestone.setVehicleInfoJson(req.getVehicleInfoJson());
        milestone.setStatus("pending");
        
        milestoneMapper.insert(milestone);
        
        // 如果合同状态是 2(已签署)，更新为 3(履约中)
        if (status == 2) {
            contractMapper.updateStatus(contractId, 3);
        }
        
        return milestone.getId();
    }

    @Override
    public List<MilestoneResponse> listByContract(Long userId, Long contractId) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new ApiException(401, "未登录");
        
        BusContract contract = contractMapper.selectById(contractId);
        if (contract == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "合同不存在");
        
        // 检查权限
        Long userCompanyId = user.getCompanyId();
        if (userCompanyId == null || 
            (!userCompanyId.equals(contract.getBuyerCompanyId()) && !userCompanyId.equals(contract.getSellerCompanyId()))) {
            throw new ApiException(403, "无权查看此合同");
        }
        
        List<BusContractMilestone> milestones = milestoneMapper.selectByContractId(contractId);
        List<MilestoneResponse> out = new ArrayList<>();
        for (BusContractMilestone m : milestones) {
            out.add(toResponse(m));
        }
        return out;
    }

    @Override
    @Transactional
    public void submit(Long userId, Long milestoneId, MilestoneSubmitRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new ApiException(401, "未登录");
        
        BusContractMilestone milestone = milestoneMapper.selectById(milestoneId);
        if (milestone == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "节点不存在");
        
        BusContract contract = contractMapper.selectById(milestone.getContractId());
        if (contract == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "合同不存在");
        
        // 检查权限
        Long userCompanyId = user.getCompanyId();
        if (userCompanyId == null || 
            (!userCompanyId.equals(contract.getBuyerCompanyId()) && !userCompanyId.equals(contract.getSellerCompanyId()))) {
            throw new ApiException(403, "无权操作此节点");
        }
        
        // 检查状态
        if (!"pending".equalsIgnoreCase(milestone.getStatus())) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "此节点已提交或已完成");
        }
        
        // 处理凭证
        String evidenceUrl = req.getEvidenceUrl();
        String evidenceJson = null;
        if (req.getEvidenceUrls() != null && !req.getEvidenceUrls().isEmpty()) {
            try {
                evidenceJson = objectMapper.writeValueAsString(req.getEvidenceUrls());
            } catch (Exception ignored) {}
        }
        
        LocalDate actualDate = req.getActualDate() != null ? req.getActualDate() : LocalDate.now();
        
        milestoneMapper.submit(milestoneId, userId, actualDate, evidenceUrl, evidenceJson, req.getRemark());
    }

    @Override
    @Transactional
    public void confirm(Long userId, Long milestoneId) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new ApiException(401, "未登录");
        
        BusContractMilestone milestone = milestoneMapper.selectById(milestoneId);
        if (milestone == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "节点不存在");
        
        BusContract contract = contractMapper.selectById(milestone.getContractId());
        if (contract == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "合同不存在");
        
        // 检查权限：只有对方才能确认
        Long userCompanyId = user.getCompanyId();
        if (userCompanyId == null || 
            (!userCompanyId.equals(contract.getBuyerCompanyId()) && !userCompanyId.equals(contract.getSellerCompanyId()))) {
            throw new ApiException(403, "无权操作此节点");
        }
        if (userId.equals(milestone.getOperatorUserId())) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "不能确认自己提交的节点");
        }
        
        // 检查状态
        if (!"submitted".equalsIgnoreCase(milestone.getStatus())) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "节点未提交或已处理");
        }
        
        milestoneMapper.confirm(milestoneId, userId);
        
        // 检查是否所有节点都已完成
        if (isAllCompleted(contract.getId())) {
            contractMapper.updateStatus(contract.getId(), 4); // 4 = 已完成
        }
    }

    @Override
    @Transactional
    public void reject(Long userId, Long milestoneId, String reason) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new ApiException(401, "未登录");
        
        BusContractMilestone milestone = milestoneMapper.selectById(milestoneId);
        if (milestone == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "节点不存在");
        
        BusContract contract = contractMapper.selectById(milestone.getContractId());
        if (contract == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "合同不存在");
        
        // 检查权限
        Long userCompanyId = user.getCompanyId();
        if (userCompanyId == null || 
            (!userCompanyId.equals(contract.getBuyerCompanyId()) && !userCompanyId.equals(contract.getSellerCompanyId()))) {
            throw new ApiException(403, "无权操作此节点");
        }
        if (userId.equals(milestone.getOperatorUserId())) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "不能拒绝自己提交的节点");
        }
        
        // 检查状态
        if (!"submitted".equalsIgnoreCase(milestone.getStatus())) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "节点未提交或已处理");
        }
        
        milestoneMapper.reject(milestoneId, userId, reason);
    }

    @Override
    public void delete(Long userId, Long milestoneId) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        BusContractMilestone milestone = milestoneMapper.selectById(milestoneId);
        if (milestone == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "节点不存在");
        
        // 只有 pending 状态可以删除
        if (!"pending".equalsIgnoreCase(milestone.getStatus())) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "已提交的节点不能删除");
        }
        
        milestoneMapper.logicalDelete(milestoneId);
    }

    @Override
    public boolean isAllCompleted(Long contractId) {
        int pendingCount = milestoneMapper.countPendingByContractId(contractId);
        return pendingCount == 0;
    }

    private MilestoneResponse toResponse(BusContractMilestone m) {
        MilestoneResponse r = new MilestoneResponse();
        r.setId(m.getId());
        r.setContractId(m.getContractId());
        r.setMilestoneType(m.getMilestoneType());
        r.setMilestoneName(m.getMilestoneName());
        r.setDescription(m.getDescription());
        r.setExpectedDate(m.getExpectedDate());
        r.setActualDate(m.getActualDate());
        r.setOperatorUserId(m.getOperatorUserId());
        r.setEvidenceUrl(m.getEvidenceUrl());
        r.setEvidenceJson(m.getEvidenceJson());
        r.setRemark(m.getRemark());
        r.setStatus(m.getStatus());
        r.setConfirmUserId(m.getConfirmUserId());
        r.setConfirmTime(m.getConfirmTime());
        r.setSortOrder(m.getSortOrder());
        r.setVehicleInfoJson(m.getVehicleInfoJson());
        r.setCreateTime(m.getCreateTime());
        
        // 获取用户名称
        if (m.getOperatorUserId() != null) {
            SysUser operator = userMapper.selectById(m.getOperatorUserId());
            if (operator != null) {
                r.setOperatorName(operator.getNickName() != null ? operator.getNickName() : operator.getUserName());
            }
        }
        if (m.getConfirmUserId() != null) {
            SysUser confirmer = userMapper.selectById(m.getConfirmUserId());
            if (confirmer != null) {
                r.setConfirmUserName(confirmer.getNickName() != null ? confirmer.getNickName() : confirmer.getUserName());
            }
        }
        
        return r;
    }
}
