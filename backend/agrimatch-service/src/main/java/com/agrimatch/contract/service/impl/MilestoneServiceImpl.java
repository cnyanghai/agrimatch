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
        milestone.setResponsibleParty(autoAssignResponsibleParty(milestone.getMilestoneType(), req.getResponsibleParty()));
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
        
        // 检查负责方：只有负责方才能提交凭证
        if (milestone.getResponsibleParty() != null && !isResponsibleParty(contract, userCompanyId, milestone.getResponsibleParty())) {
            String partyName = "buyer".equals(milestone.getResponsibleParty()) ? "买方" : "卖方";
            throw new ApiException(403, "此节点由" + partyName + "负责提交");
        }

        // 检查状态：pending 或 rejected 可以(重新)提交
        String st = milestone.getStatus();
        if (!"pending".equalsIgnoreCase(st) && !"rejected".equalsIgnoreCase(st)) {
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
        // 公司级校验：负责方的公司不能确认自己公司提交的节点
        if (milestone.getResponsibleParty() != null && isResponsibleParty(contract, userCompanyId, milestone.getResponsibleParty())) {
            throw new ApiException(403, "不能确认本方负责的节点，需对方确认");
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
        // 公司级校验：负责方的公司不能拒绝自己公司提交的节点
        if (milestone.getResponsibleParty() != null && isResponsibleParty(contract, userCompanyId, milestone.getResponsibleParty())) {
            throw new ApiException(403, "不能拒绝本方负责的节点，需对方操作");
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

    /**
     * 根据节点类型自动分配负责方
     */
    private String autoAssignResponsibleParty(String type, String userSpecified) {
        if (type == null) return userSpecified;
        switch (type.toUpperCase()) {
            case "SHIP":    return "seller";
            case "RECEIVE": return "buyer";
            case "PAY":     return "buyer";
            case "INSPECT": return "seller";
            case "CUSTOM":  return userSpecified; // 由创建者指定
            default:        return userSpecified;
        }
    }

    /**
     * 检查当前公司是否为该节点的负责方
     */
    private boolean isResponsibleParty(BusContract contract, Long companyId, String party) {
        if (party == null || companyId == null) return false;
        if ("buyer".equals(party)) {
            return companyId.equals(contract.getBuyerCompanyId());
        } else if ("seller".equals(party)) {
            return companyId.equals(contract.getSellerCompanyId());
        }
        return false;
    }

    /**
     * 检查当前公司是否为该节点负责方的对方
     */
    private boolean isOppositeParty(BusContract contract, Long companyId, String party) {
        if (party == null || companyId == null) return false;
        if ("buyer".equals(party)) {
            // 负责方是买方，对方应该是卖方
            return companyId.equals(contract.getSellerCompanyId());
        } else if ("seller".equals(party)) {
            // 负责方是卖方，对方应该是买方
            return companyId.equals(contract.getBuyerCompanyId());
        }
        return false;
    }

    @Override
    @Transactional
    public List<MilestoneResponse> generateStandardMilestones(Long userId, Long contractId) {
        if (userId == null) throw new ApiException(401, "未登录");

        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new ApiException(401, "未登录");

        BusContract contract = contractMapper.selectById(contractId);
        if (contract == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "合同不存在");

        // 检查权限
        Long userCompanyId = user.getCompanyId();
        if (userCompanyId == null ||
            (!userCompanyId.equals(contract.getBuyerCompanyId()) && !userCompanyId.equals(contract.getSellerCompanyId()))) {
            throw new ApiException(403, "无权操作此合同");
        }

        // 检查状态：只有 2(已签署) 或 3(履约中)
        Integer status = contract.getStatus();
        if (status == null || (status != 2 && status != 3)) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "合同未生效或已完成");
        }

        // 检查是否已有节点
        int existingCount = milestoneMapper.countTotalByContractId(contractId);
        if (existingCount > 0) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "已有履约节点，无法自动生成");
        }

        // 根据付款方式生成标准流程
        String paymentMethod = contract.getPaymentMethod();
        List<BusContractMilestone> milestoneList = buildStandardMilestones(contractId, paymentMethod);

        if (!milestoneList.isEmpty()) {
            milestoneMapper.batchInsert(milestoneList);
        }

        // 如果合同状态是 2(已签署)，更新为 3(履约中)
        if (status == 2) {
            contractMapper.updateStatus(contractId, 3);
        }

        // 返回生成的节点列表
        List<BusContractMilestone> saved = milestoneMapper.selectByContractId(contractId);
        List<MilestoneResponse> out = new ArrayList<>();
        for (BusContractMilestone m : saved) {
            out.add(toResponse(m));
        }
        return out;
    }

    /**
     * 根据付款方式构建标准节点列表
     */
    private List<BusContractMilestone> buildStandardMilestones(Long contractId, String paymentMethod) {
        List<BusContractMilestone> list = new ArrayList<>();

        if ("01".equals(paymentMethod) || "款到发货".equals(paymentMethod)) {
            // 款到发货: 付款(buyer) → 发货(seller) → 收货(buyer)
            list.add(newMilestone(contractId, "PAY", "buyer", "付款", "买方完成付款", 1));
            list.add(newMilestone(contractId, "SHIP", "seller", "发货", "卖方发货", 2));
            list.add(newMilestone(contractId, "RECEIVE", "buyer", "收货", "买方确认收货", 3));
        } else if ("02".equals(paymentMethod) || "货到付款".equals(paymentMethod)) {
            // 货到付款: 发货(seller) → 收货(buyer) → 付款(buyer)
            list.add(newMilestone(contractId, "SHIP", "seller", "发货", "卖方发货", 1));
            list.add(newMilestone(contractId, "RECEIVE", "buyer", "收货", "买方确认收货", 2));
            list.add(newMilestone(contractId, "PAY", "buyer", "付款", "买方完成付款", 3));
        } else if ("06".equals(paymentMethod) || "预付定金".equals(paymentMethod)) {
            // 预付定金: 定金(buyer) → 发货(seller) → 收货(buyer) → 尾款(buyer)
            list.add(newMilestone(contractId, "PAY", "buyer", "支付定金", "买方支付定金", 1));
            list.add(newMilestone(contractId, "SHIP", "seller", "发货", "卖方发货", 2));
            list.add(newMilestone(contractId, "RECEIVE", "buyer", "收货", "买方确认收货", 3));
            list.add(newMilestone(contractId, "PAY", "buyer", "支付尾款", "买方支付尾款", 4));
        } else {
            // 账期(03/04)、分期(05)、其他/默认: 发货(seller) → 收货(buyer) → 付款(buyer)
            list.add(newMilestone(contractId, "SHIP", "seller", "发货", "卖方发货", 1));
            list.add(newMilestone(contractId, "RECEIVE", "buyer", "收货", "买方确认收货", 2));
            list.add(newMilestone(contractId, "PAY", "buyer", "付款", "买方完成付款", 3));
        }

        return list;
    }

    private BusContractMilestone newMilestone(Long contractId, String type, String party, String name, String desc, int order) {
        BusContractMilestone m = new BusContractMilestone();
        m.setContractId(contractId);
        m.setMilestoneType(type);
        m.setResponsibleParty(party);
        m.setMilestoneName(name);
        m.setDescription(desc);
        m.setSortOrder(order);
        m.setStatus("pending");
        return m;
    }

    private MilestoneResponse toResponse(BusContractMilestone m) {
        MilestoneResponse r = new MilestoneResponse();
        r.setId(m.getId());
        r.setContractId(m.getContractId());
        r.setMilestoneType(m.getMilestoneType());
        r.setResponsibleParty(m.getResponsibleParty());
        r.setMilestoneName(m.getMilestoneName());
        r.setDescription(m.getDescription());
        r.setExpectedDate(m.getExpectedDate());
        r.setActualDate(m.getActualDate());
        r.setOperatorUserId(m.getOperatorUserId());
        r.setEvidenceUrl(m.getEvidenceUrl());
        r.setEvidenceJson(m.getEvidenceJson());
        r.setRemark(m.getRemark());
        r.setRejectReason(m.getRejectReason());
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
