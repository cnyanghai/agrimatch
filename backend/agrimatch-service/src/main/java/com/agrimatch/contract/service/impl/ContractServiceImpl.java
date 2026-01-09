package com.agrimatch.contract.service.impl;

import com.agrimatch.chat.domain.BusChatConversation;
import com.agrimatch.chat.domain.BusChatMessage;
import com.agrimatch.chat.dto.ChatMessageResponse;
import com.agrimatch.chat.event.ContractMessageEvent;
import com.agrimatch.chat.event.MessageUpdateEvent;
import com.agrimatch.chat.mapper.ChatMapper;
import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.company.domain.BusCompany;
import com.agrimatch.company.mapper.CompanyMapper;
import com.agrimatch.contract.domain.BusCompanySeal;
import com.agrimatch.contract.domain.BusContract;
import com.agrimatch.contract.domain.BusContractChangeLog;
import com.agrimatch.contract.domain.BusContractSignature;
import com.agrimatch.contract.dto.*;
import com.agrimatch.contract.mapper.CompanySealMapper;
import com.agrimatch.contract.mapper.ContractChangeLogMapper;
import com.agrimatch.contract.mapper.ContractMapper;
import com.agrimatch.contract.mapper.ContractSignatureMapper;
import com.agrimatch.contract.service.ContractService;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ContractServiceImpl implements ContractService {
    private static final Logger log = LoggerFactory.getLogger(ContractServiceImpl.class);
    
    private final ContractMapper contractMapper;
    private final ContractSignatureMapper signatureMapper;
    private final ContractChangeLogMapper changeLogMapper;
    private final CompanySealMapper sealMapper;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;
    private final ChatMapper chatMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public ContractServiceImpl(ContractMapper contractMapper,
                               ContractSignatureMapper signatureMapper,
                               ContractChangeLogMapper changeLogMapper,
                               CompanySealMapper sealMapper,
                               UserMapper userMapper,
                               CompanyMapper companyMapper,
                               ChatMapper chatMapper,
                               ObjectMapper objectMapper,
                               ApplicationEventPublisher eventPublisher) {
        this.contractMapper = contractMapper;
        this.signatureMapper = signatureMapper;
        this.changeLogMapper = changeLogMapper;
        this.sealMapper = sealMapper;
        this.userMapper = userMapper;
        this.companyMapper = companyMapper;
        this.chatMapper = chatMapper;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Long create(Long userId, ContractCreateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        SysUser u = userMapper.selectById(userId);
        if (u == null) throw new ApiException(401, "未登录");
        if (u.getCompanyId() == null) throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "请先完善公司档案（绑定公司）");

        BusContract c = new BusContract();
        c.setContractNo(req.getContractNo().trim());
        c.setBuyerCompanyId(u.getCompanyId()); // 默认当前用户公司为买方
        c.setProductName(emptyToNull(req.getProductName()));
        c.setQuantity(req.getQuantity());
        c.setUnit(emptyToNull(req.getUnit()));
        c.setUnitPrice(req.getUnitPrice());
        c.setTotalAmount(calcTotal(req.getQuantity(), req.getUnitPrice()));
        c.setDeliveryDate(req.getDeliveryDate());
        c.setDeliveryAddress(emptyToNull(req.getDeliveryAddress()));
        c.setPaymentMethod(emptyToNull(req.getPaymentMethod()));
        c.setStatus(0); // 草稿

        int rows = contractMapper.insert(c);
        if (rows != 1 || c.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);
        
        // 记录创建日志
        logChange(c.getId(), "CREATE", "创建合同", null, null, userId);
        
        return c.getId();
    }

    @Override
    @Transactional
    public Long createFromQuote(Long userId, ContractFromQuoteRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        SysUser currentUser = userMapper.selectById(userId);
        if (currentUser == null) throw new ApiException(401, "未登录");
        if (currentUser.getCompanyId() == null) throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "请先完善公司档案");

        // 获取报价消息
        BusChatMessage quoteMsg = chatMapper.selectMessageById(req.getQuoteMessageId());
        if (quoteMsg == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "报价消息不存在");
        if (!"QUOTE".equalsIgnoreCase(quoteMsg.getMsgType())) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "消息类型不是报价");
        }
        if (!"ACCEPTED".equalsIgnoreCase(quoteMsg.getQuoteStatus())) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "报价未被确认，无法创建合同");
        }
        
        // 检查是否已经从此报价创建过合同
        BusContract existing = contractMapper.selectByQuoteMessageId(req.getQuoteMessageId());
        if (existing != null) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "此报价已创建过合同");
        }

        // 解析报价单 payloadJson
        QuoteData quoteData = parseQuotePayload(quoteMsg.getPayloadJson());
        
        // 从会话标的快照中获取产品信息作为备选
        BusChatConversation conversation = chatMapper.selectConversationById(quoteMsg.getConversationId());
        if (conversation != null && conversation.getSubjectSnapshotJson() != null) {
            try {
                JsonNode snapshot = objectMapper.readTree(conversation.getSubjectSnapshotJson());
                // 产品名称
                if (quoteData.productName == null && snapshot.has("productName")) {
                    quoteData.productName = snapshot.get("productName").asText();
                }
                if (quoteData.productName == null && snapshot.has("categoryName")) {
                    quoteData.productName = snapshot.get("categoryName").asText();
                }
                if (quoteData.productName == null && snapshot.has("title")) {
                    quoteData.productName = snapshot.get("title").asText();
                }
                // 分类名称
                if (quoteData.categoryName == null && snapshot.has("categoryName")) {
                    quoteData.categoryName = snapshot.get("categoryName").asText();
                }
                // 数量（从标的快照补充）
                if (quoteData.quantity == null && snapshot.has("quantity") && !snapshot.get("quantity").isNull()) {
                    try {
                        quoteData.quantity = new BigDecimal(snapshot.get("quantity").asText());
                    } catch (Exception ignored) {}
                }
                // 价格（从标的快照补充，exFactoryPrice 或 expectedPrice）
                if (quoteData.unitPrice == null) {
                    if (snapshot.has("exFactoryPrice") && !snapshot.get("exFactoryPrice").isNull()) {
                        try {
                            quoteData.unitPrice = new BigDecimal(snapshot.get("exFactoryPrice").asText());
                        } catch (Exception ignored) {}
                    } else if (snapshot.has("expectedPrice") && !snapshot.get("expectedPrice").isNull()) {
                        try {
                            quoteData.unitPrice = new BigDecimal(snapshot.get("expectedPrice").asText());
                        } catch (Exception ignored) {}
                    }
                }
                // 交付地址
                if (quoteData.deliveryPlace == null && snapshot.has("shipAddress")) {
                    quoteData.deliveryPlace = snapshot.get("shipAddress").asText();
                }
                if (quoteData.deliveryPlace == null && snapshot.has("purchaseAddress")) {
                    quoteData.deliveryPlace = snapshot.get("purchaseAddress").asText();
                }
                // 交付方式
                if (quoteData.deliveryMode == null && snapshot.has("deliveryMode")) {
                    quoteData.deliveryMode = snapshot.get("deliveryMode").asText();
                }
                // 产品参数
                if (quoteData.paramsJson == null && snapshot.has("paramsJson")) {
                    quoteData.paramsJson = snapshot.get("paramsJson").asText();
                }
            } catch (Exception ignored) {}
        }
        
        // 如果产品名称仍为空，设置默认值
        if (quoteData.productName == null || quoteData.productName.isBlank()) {
            quoteData.productName = "商品";
        }

        // 获取双方用户信息
        Long fromUserId = quoteMsg.getFromUserId();
        Long toUserId = quoteMsg.getToUserId();
        SysUser fromUser = userMapper.selectById(fromUserId);
        SysUser toUser = userMapper.selectById(toUserId);
        
        if (fromUser == null || toUser == null) {
            throw new ApiException(ResultCode.NOT_FOUND.getCode(), "用户信息不存在");
        }

        // 获取公司ID
        Long fromCompanyId = fromUser.getCompanyId();
        Long toCompanyId = toUser.getCompanyId();

        // 校验双方公司信息是否完整
        if (fromCompanyId == null) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), 
                "报价方尚未完善公司信息，无法创建合同");
        }
        if (toCompanyId == null) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), 
                "您尚未完善公司信息，请先前往个人中心完善资料");
        }

        // 确定买卖双方（假设：接收报价并确认的一方是买方）
        Long buyerCompanyId = toCompanyId;
        Long sellerCompanyId = fromCompanyId;

        // 校验报价单必填字段 (针对基差合同，如果 unitPrice 为空，但 basisPrice 存在，则视为合法)
        if (quoteData.quantity == null || quoteData.quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), 
                "报价单中缺少数量信息，无法创建合同");
        }
        
        boolean isBasis = quoteData.basisPrice != null && StringUtils.hasText(quoteData.contractCode);
        if (!isBasis && (quoteData.unitPrice == null || quoteData.unitPrice.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), 
                "报价单中缺少价格信息，无法创建合同");
        }

        // 生成合同编号
        String contractNo = generateContractNo();

        // 构建合同
        BusContract contract = new BusContract();
        contract.setQuoteMessageId(req.getQuoteMessageId());
        contract.setConversationId(quoteMsg.getConversationId());
        contract.setContractNo(contractNo);
        
        contract.setBuyerCompanyId(buyerCompanyId);
        contract.setSellerCompanyId(sellerCompanyId);

        contract.setProductName(quoteData.productName);
        contract.setCategoryName(quoteData.categoryName);
        contract.setQuantity(quoteData.quantity);
        contract.setUnit(quoteData.unit != null ? quoteData.unit : "吨");
        contract.setUnitPrice(quoteData.unitPrice);
        contract.setParamsJson(quoteData.paramsJson);
        contract.setBasisPrice(quoteData.basisPrice);
        contract.setContractCode(quoteData.contractCode);
        contract.setTotalAmount(calcTotal(quoteData.quantity, quoteData.unitPrice));

        // 使用请求中的覆盖值或从报价中获取
        LocalDate parsedDeliveryDate = req.parseDeliveryDate();
        contract.setDeliveryDate(parsedDeliveryDate != null ? parsedDeliveryDate : quoteData.arrivalDate);
        contract.setDeliveryAddress(req.getDeliveryAddress() != null ? req.getDeliveryAddress() : quoteData.deliveryPlace);
        contract.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : quoteData.paymentMethod);
        contract.setDeliveryMode(quoteData.deliveryMode);
        
        // 生成默认条款 JSON
        contract.setTermsJson(generateDefaultTermsJson(contract));

        contract.setStatus(0); // 草稿

        int rows = contractMapper.insert(contract);
        if (rows != 1 || contract.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);

        // 记录创建日志
        logChange(contract.getId(), "CREATE", "从报价单创建合同", null, null, userId);
        
        // 发送 CONTRACT 消息到聊天
        sendContractMessage(userId, contract, conversation);

        return contract.getId();
    }
    
    /**
     * 发送合同消息到聊天，并通过 WebSocket 广播给双方
     */
    private void sendContractMessage(Long fromUserId, BusContract contract, BusChatConversation conversation) {
        if (conversation == null) {
            log.warn("sendContractMessage: conversation is null, skip");
            return;
        }
        
        try {
            // 确定收件人
            Long toUserId = fromUserId.equals(conversation.getAUserId()) 
                ? conversation.getBUserId() 
                : conversation.getAUserId();
            
            // 获取公司名称
            String buyerName = "买方";
            String sellerName = "卖方";
            if (contract.getBuyerCompanyId() != null) {
                BusCompany buyer = companyMapper.selectById(contract.getBuyerCompanyId());
                if (buyer != null) buyerName = buyer.getCompanyName();
            }
            if (contract.getSellerCompanyId() != null) {
                BusCompany seller = companyMapper.selectById(contract.getSellerCompanyId());
                if (seller != null) sellerName = seller.getCompanyName();
            }
            
            // 构建 payloadJson
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("contractId", contract.getId());
            payload.put("contractNo", contract.getContractNo());
            payload.put("productName", contract.getProductName());
            payload.put("quantity", contract.getQuantity());
            payload.put("unit", contract.getUnit());
            payload.put("unitPrice", contract.getUnitPrice());
            payload.put("basisPrice", contract.getBasisPrice());
            payload.put("contractCode", contract.getContractCode());
            payload.put("totalAmount", contract.getTotalAmount());
            payload.put("buyerCompanyId", contract.getBuyerCompanyId());
            payload.put("buyerCompanyName", buyerName);
            payload.put("sellerCompanyId", contract.getSellerCompanyId());
            payload.put("sellerCompanyName", sellerName);
            payload.put("status", contract.getStatus());
            payload.put("buyerSigned", false);
            payload.put("sellerSigned", false);
            
            String payloadJson = objectMapper.writeValueAsString(payload);
            String content = "【合同】" + contract.getContractNo() + " - " + contract.getProductName();
            
            // 创建消息
            BusChatMessage msg = new BusChatMessage();
            msg.setConversationId(conversation.getId());
            msg.setFromUserId(fromUserId);
            msg.setToUserId(toUserId);
            msg.setMsgType("CONTRACT");
            msg.setContent(content);
            msg.setPayloadJson(payloadJson);
            msg.setIsRead(0);
            
            int rows = chatMapper.insertMessage(msg);
            if (rows != 1 || msg.getId() == null) {
                log.error("sendContractMessage: insert message failed");
                return;
            }
            
            // 更新会话最后消息
            chatMapper.updateConversationLast(conversation.getId(), msg.getId(), content);
            
            log.info("sendContractMessage: inserted CONTRACT message id={} for contract {}", msg.getId(), contract.getContractNo());
            
            // 构建 ChatMessageResponse 用于 WebSocket 广播
            ChatMessageResponse response = new ChatMessageResponse();
            response.setId(msg.getId());
            response.setConversationId(conversation.getId());
            response.setFromUserId(fromUserId);
            response.setToUserId(toUserId);
            response.setMsgType("CONTRACT");
            response.setContent(content);
            response.setPayloadJson(payloadJson);
            response.setRead(false);
            response.setCreateTime(java.time.LocalDateTime.now());
            
            // 发布事件，触发 WebSocket 广播给双方用户
            eventPublisher.publishEvent(new ContractMessageEvent(
                this, 
                conversation.getId(), 
                conversation.getAUserId(), 
                conversation.getBUserId(), 
                response
            ));
            
            log.info("sendContractMessage: published ContractMessageEvent to broadcast");
            
        } catch (Exception e) {
            log.error("sendContractMessage failed: {}", e.getMessage(), e);
        }
    }

    @Override
    public ContractResponse getById(Long viewerUserId, Long id) {
        if (viewerUserId == null) throw new ApiException(401, "未登录");
        
        // 使用详情查询，关联公司信息
        Map<String, Object> detail = contractMapper.selectDetailById(id);
        if (detail == null) throw new ApiException(ResultCode.NOT_FOUND);
        
        // 检查权限
        SysUser viewer = userMapper.selectById(viewerUserId);
        if (viewer == null) throw new ApiException(401, "未登录");
        
        Long buyerCompanyId = getLongValue(detail, "buyer_company_id");
        Long sellerCompanyId = getLongValue(detail, "seller_company_id");
        
        Long viewerCompanyId = viewer.getCompanyId();
        if (viewerCompanyId == null || 
            (!viewerCompanyId.equals(buyerCompanyId) && !viewerCompanyId.equals(sellerCompanyId))) {
            throw new ApiException(403, "无权查看此合同");
        }
        
        return toDetailResponse(detail, viewerUserId);
    }

    @Override
    public List<ContractResponse> list(Long viewerUserId, ContractQuery q) {
        if (viewerUserId == null) throw new ApiException(401, "未登录");
        SysUser u = userMapper.selectById(viewerUserId);
        if (u == null) throw new ApiException(401, "未登录");

        if (q == null) q = new ContractQuery();
        
        // 查询当前用户公司相关的合同
        if (u.getCompanyId() != null) {
            q.setCompanyId(u.getCompanyId());
        }
        
        if (q.getKeyword() != null && q.getKeyword().length() > 64) {
            q.setKeyword(q.getKeyword().substring(0, 64));
        }

        List<BusContract> list = contractMapper.selectList(q);
        List<ContractResponse> out = new ArrayList<>();
        for (BusContract c : list) out.add(toResponse(c, viewerUserId));
        return out;
    }

    @Override
    public void update(Long userId, Long id, ContractUpdateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        BusContract existing = contractMapper.selectById(id);
        if (existing == null) throw new ApiException(ResultCode.NOT_FOUND);
        
        // 只有草稿状态可以修改
        if (existing.getStatus() != null && existing.getStatus() != 0) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "只有草稿状态的合同可以修改");
        }
        
        BusContract c = new BusContract();
        c.setId(id);
        c.setProductName(emptyToNull(req.getProductName()));
        c.setQuantity(req.getQuantity());
        c.setUnit(emptyToNull(req.getUnit()));
        c.setUnitPrice(req.getUnitPrice());
        c.setTotalAmount(calcTotal(req.getQuantity(), req.getUnitPrice()));
        c.setDeliveryDate(req.getDeliveryDate());
        c.setDeliveryAddress(emptyToNull(req.getDeliveryAddress()));
        c.setPaymentMethod(emptyToNull(req.getPaymentMethod()));

        int rows = contractMapper.update(c);
        if (rows != 1) throw new ApiException(ResultCode.NOT_FOUND);
        
        logChange(id, "UPDATE", "修改合同内容", null, null, userId);
    }

    @Override
    public void delete(Long userId, Long id) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        BusContract existing = contractMapper.selectById(id);
        if (existing == null) throw new ApiException(ResultCode.NOT_FOUND);
        
        // 只有草稿状态可以删除
        if (existing.getStatus() != null && existing.getStatus() != 0) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "只有草稿状态的合同可以删除");
        }
        
        int rows = contractMapper.logicalDelete(id);
        if (rows != 1) throw new ApiException(ResultCode.NOT_FOUND);
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long contractId, String reason) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        BusContract c = contractMapper.selectById(contractId);
        if (c == null) throw new ApiException(ResultCode.NOT_FOUND);
        
        // 权限校验：通过会话验证用户是否为参与者
        if (c.getConversationId() != null) {
            BusChatConversation conversation = chatMapper.selectConversationById(c.getConversationId());
            if (conversation != null) {
                boolean isParticipant = userId.equals(conversation.getAUserId()) || userId.equals(conversation.getBUserId());
                if (!isParticipant) {
                    throw new ApiException(403, "无权操作此合同");
                }
            }
        }
        
        // 状态校验：只有草稿(0)或待签署(1)状态可以取消
        Integer status = c.getStatus();
        if (status == null || (status != 0 && status != 1)) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "只有草稿或待签署状态的合同可以取消");
        }
        
        String beforeStatus = String.valueOf(status);
        contractMapper.updateStatus(contractId, 5); // 5 = 已取消
        
        String desc = "取消合同";
        if (reason != null && !reason.isBlank()) {
            desc += "，原因：" + reason;
        }
        logChange(contractId, "STATUS", desc, beforeStatus, "5", userId);
    }

    @Override
    @Transactional
    public void sendForSigning(Long userId, Long contractId) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        BusContract c = contractMapper.selectById(contractId);
        if (c == null) throw new ApiException(ResultCode.NOT_FOUND);
        
        if (c.getStatus() != null && c.getStatus() != 0) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "只有草稿状态的合同可以发送签署");
        }
        
        contractMapper.updateStatus(contractId, 1); // 1 = 待签署
        logChange(contractId, "STATUS", "发送合同待签署", "0", "1", userId);
    }

    @Override
    @Transactional
    public void sign(Long userId, Long contractId, ContractSignRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new ApiException(401, "未登录");
        
        BusContract c = contractMapper.selectById(contractId);
        if (c == null) throw new ApiException(ResultCode.NOT_FOUND);
        
        // 检查状态：1=待签署, 可以签署
        if (c.getStatus() == null || (c.getStatus() != 1 && c.getStatus() != 2)) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "当前状态不可签署");
        }
        
        // 确定签署方
        String partyType;
        Long companyId = user.getCompanyId();
        if (companyId != null && companyId.equals(c.getBuyerCompanyId())) {
            partyType = "buyer";
        } else if (companyId != null && companyId.equals(c.getSellerCompanyId())) {
            partyType = "seller";
        } else {
            throw new ApiException(403, "您不是此合同的签署方");
        }
        
        // 检查是否已签署
        BusContractSignature existingSig = signatureMapper.selectByContractAndParty(contractId, partyType);
        if (existingSig != null) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "您已签署过此合同");
        }
        
        // 创建签署记录
        BusContractSignature sig = new BusContractSignature();
        sig.setContractId(contractId);
        sig.setUserId(userId);
        sig.setCompanyId(companyId);
        sig.setPartyType(partyType);
        sig.setSignType(req.getSignType());
        sig.setSignerName(req.getSignerName());
        
        // 处理公章
        if (req.getSealId() != null) {
            BusCompanySeal seal = sealMapper.selectById(req.getSealId());
            if (seal != null) {
                sig.setSealId(seal.getId());
                sig.setSealUrl(seal.getSealUrl());
            }
        }
        
        signatureMapper.insert(sig);
        
        // 更新合同签署时间
        BusContract update = new BusContract();
        update.setId(contractId);
        if ("buyer".equals(partyType)) {
            update.setBuyerSignTime(LocalDateTime.now());
        } else {
            update.setSellerSignTime(LocalDateTime.now());
        }
        update.setStatus(1); // 保持待签署状态
        contractMapper.update(update);
        
        logChange(contractId, "SIGN", partyType + " 签署合同", null, null, userId);
        
        // 检查是否双方都已签署
        checkAndUpdateSignStatus(contractId, userId);
        
        // 更新聊天消息中的合同状态
        updateContractMessageStatus(contractId);
    }

    @Override
    @Transactional
    public void checkAndUpdateSignStatus(Long contractId, Long userId) {
        int signCount = signatureMapper.countByContractId(contractId);
        if (signCount >= 2) {
            // 双方都已签署，更新状态为已签署
            contractMapper.updateStatus(contractId, 2); // 2 = 已签署
            logChange(contractId, "STATUS", "双方签署完成，合同生效", "1", "2", userId);
            
            // 发送签署完成系统消息
            sendSignCompleteMessage(contractId);
        }
    }
    
    /**
     * 发送签署完成系统消息到聊天
     */
    private void sendSignCompleteMessage(Long contractId) {
        try {
            // 查找该合同对应的 CONTRACT 消息，获取会话信息
            BusChatMessage contractMsg = chatMapper.selectContractMessageByContractId(contractId);
            if (contractMsg == null) {
                log.warn("sendSignCompleteMessage: No CONTRACT message found for contract {}", contractId);
                return;
            }
            
            BusChatConversation conversation = chatMapper.selectConversationById(contractMsg.getConversationId());
            if (conversation == null) {
                log.warn("sendSignCompleteMessage: Conversation not found for contract {}", contractId);
                return;
            }
            
            BusContract contract = contractMapper.selectById(contractId);
            if (contract == null) {
                log.warn("sendSignCompleteMessage: Contract {} not found", contractId);
                return;
            }
            
            // 创建系统消息
            String content = "🎉 合同【" + contract.getContractNo() + "】双方签署完成，已正式生效！";
            
            BusChatMessage msg = new BusChatMessage();
            msg.setConversationId(conversation.getId());
            msg.setFromUserId(0L); // 系统消息使用 0 作为发送者
            msg.setToUserId(conversation.getAUserId()); // 这里可以是任意一方，实际上是广播给双方
            msg.setMsgType("SYSTEM");
            msg.setContent(content);
            msg.setIsRead(0);
            
            int rows = chatMapper.insertMessage(msg);
            if (rows != 1 || msg.getId() == null) {
                log.error("sendSignCompleteMessage: insert message failed");
                return;
            }
            
            // 更新会话最后消息
            chatMapper.updateConversationLast(conversation.getId(), msg.getId(), content);
            
            log.info("sendSignCompleteMessage: sent system message for contract {}", contractId);
            
            // 构建 ChatMessageResponse 用于 WebSocket 广播
            ChatMessageResponse response = new ChatMessageResponse();
            response.setId(msg.getId());
            response.setConversationId(conversation.getId());
            response.setFromUserId(0L);
            response.setToUserId(conversation.getAUserId());
            response.setMsgType("SYSTEM");
            response.setContent(content);
            response.setRead(false);
            response.setCreateTime(java.time.LocalDateTime.now());
            
            // 发布事件，触发 WebSocket 广播给双方用户
            eventPublisher.publishEvent(new ContractMessageEvent(
                this, 
                conversation.getId(), 
                conversation.getAUserId(), 
                conversation.getBUserId(), 
                response
            ));
            
        } catch (Exception e) {
            log.error("sendSignCompleteMessage failed for contract {}: {}", contractId, e.getMessage(), e);
        }
    }

    /**
     * 更新聊天消息中的合同状态，并广播给双方用户
     */
    private void updateContractMessageStatus(Long contractId) {
        try {
            // 查找该合同对应的 CONTRACT 消息
            BusChatMessage msg = chatMapper.selectContractMessageByContractId(contractId);
            if (msg == null) {
                log.warn("updateContractMessageStatus: No CONTRACT message found for contract {}", contractId);
                return;
            }
            
            // 获取最新的合同数据
            BusContract contract = contractMapper.selectById(contractId);
            if (contract == null) {
                log.warn("updateContractMessageStatus: Contract {} not found", contractId);
                return;
            }
            
            // 获取签署状态
            BusContractSignature sigBuyer = signatureMapper.selectByContractAndParty(contractId, "buyer");
            BusContractSignature sigSeller = signatureMapper.selectByContractAndParty(contractId, "seller");
            boolean buyerSigned = sigBuyer != null;
            boolean sellerSigned = sigSeller != null;
            
            // 获取公司名称
            String buyerName = "买方";
            String sellerName = "卖方";
            if (contract.getBuyerCompanyId() != null) {
                BusCompany buyer = companyMapper.selectById(contract.getBuyerCompanyId());
                if (buyer != null) buyerName = buyer.getCompanyName();
            }
            if (contract.getSellerCompanyId() != null) {
                BusCompany seller = companyMapper.selectById(contract.getSellerCompanyId());
                if (seller != null) sellerName = seller.getCompanyName();
            }
            
            // 构建更新后的 payloadJson
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("contractId", contract.getId());
            payload.put("contractNo", contract.getContractNo());
            payload.put("productName", contract.getProductName());
            payload.put("quantity", contract.getQuantity());
            payload.put("unit", contract.getUnit());
            payload.put("unitPrice", contract.getUnitPrice());
            payload.put("basisPrice", contract.getBasisPrice());
            payload.put("contractCode", contract.getContractCode());
            payload.put("totalAmount", contract.getTotalAmount());
            payload.put("buyerCompanyId", contract.getBuyerCompanyId());
            payload.put("buyerCompanyName", buyerName);
            payload.put("sellerCompanyId", contract.getSellerCompanyId());
            payload.put("sellerCompanyName", sellerName);
            payload.put("status", contract.getStatus());
            payload.put("buyerSigned", buyerSigned);
            payload.put("sellerSigned", sellerSigned);
            
            String payloadJson = objectMapper.writeValueAsString(payload);
            
            // 更新消息
            chatMapper.updateMessagePayload(msg.getId(), payloadJson);
            log.info("updateContractMessageStatus: Updated message {} payload for contract {}", msg.getId(), contractId);
            
            // 获取会话双方用户
            BusChatConversation conversation = chatMapper.selectConversationById(msg.getConversationId());
            if (conversation != null) {
                // 发布消息更新事件
                eventPublisher.publishEvent(new MessageUpdateEvent(
                    this,
                    msg.getId(),
                    msg.getConversationId(),
                    conversation.getAUserId(),
                    conversation.getBUserId(),
                    payloadJson
                ));
                log.info("updateContractMessageStatus: Published MessageUpdateEvent for message {}", msg.getId());
            }
            
        } catch (Exception e) {
            log.error("updateContractMessageStatus failed for contract {}: {}", contractId, e.getMessage(), e);
        }
    }

    @Override
    public byte[] generatePdf(Long viewerUserId, Long id) {
        if (viewerUserId == null) throw new ApiException(401, "未登录");
        BusContract c = contractMapper.selectById(id);
        if (c == null) throw new ApiException(ResultCode.NOT_FOUND);

        // 获取公司名称
        String buyerName = "买方";
        String sellerName = "卖方";
        if (c.getBuyerCompanyId() != null) {
            BusCompany buyer = companyMapper.selectById(c.getBuyerCompanyId());
            if (buyer != null) buyerName = buyer.getCompanyName();
        }
        if (c.getSellerCompanyId() != null) {
            BusCompany seller = companyMapper.selectById(c.getSellerCompanyId());
            if (seller != null) sellerName = seller.getCompanyName();
        }

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 56;
                float y = page.getMediaBox().getHeight() - margin;
                float x = margin;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(x, y);
                cs.showText("Purchase Contract - " + safeAscii(c.getContractNo()));
                cs.endText();

                y -= 28;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.newLineAtOffset(x, y);
                cs.showText("Contract No: " + safeAscii(c.getContractNo()));
                cs.endText();

                y -= 18;
                y = writeBlock(cs, x, y, "Buyer: " + safeAscii(buyerName));
                y = writeBlock(cs, x, y, "Seller: " + safeAscii(sellerName));
                y = writeBlock(cs, x, y, "Product: " + safeAscii(nvl(c.getProductName(), "-")));

                String qty = c.getQuantity() != null ? c.getQuantity().toPlainString() : "-";
                String unit = nvl(c.getUnit(), "-");
                y = writeBlock(cs, x, y, "Quantity: " + qty + " " + safeAscii(unit));
                y = writeBlock(cs, x, y, "Unit Price: " + (c.getUnitPrice() != null ? c.getUnitPrice().toPlainString() : "-"));
                y = writeBlock(cs, x, y, "Total Amount: " + (c.getTotalAmount() != null ? c.getTotalAmount().toPlainString() : "-"));
                y = writeBlock(cs, x, y, "Delivery: " + (c.getDeliveryAddress() != null ? safeAscii(c.getDeliveryAddress()) : "-"));
                y = writeBlock(cs, x, y, "Payment: " + (c.getPaymentMethod() != null ? safeAscii(c.getPaymentMethod()) : "-"));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            byte[] bytes = baos.toByteArray();

            String hash = sha256Hex(bytes);
            String tag = "SHA256:" + hash;
            
            BusContract upd = new BusContract();
            upd.setId(c.getId());
            upd.setPdfHash(tag);
            contractMapper.update(upd);
            
            return bytes;
        } catch (Exception e) {
            throw new ApiException(ResultCode.SERVER_ERROR.getCode(), "PDF 生成失败: " + e.getMessage());
        }
    }

    // ==================== Helper Methods ====================

    private static class QuoteData {
        String productName;
        String categoryName;
        BigDecimal quantity;
        String unit;
        BigDecimal unitPrice;
        String deliveryPlace;
        LocalDate arrivalDate;
        String paymentMethod;
        String deliveryMode;
        String paramsJson;
        BigDecimal basisPrice;
        String contractCode;
    }

    private QuoteData parseQuotePayload(String payloadJson) {
        QuoteData data = new QuoteData();
        if (!StringUtils.hasText(payloadJson)) return data;

        try {
            JsonNode root = objectMapper.readTree(payloadJson);
            JsonNode fields = root.has("fields") ? root.get("fields") : root;

            // 1. 解析价格 (优先取 price，基差报价取 referencePrice)
            data.unitPrice = parseBigDecimal(fields, "price");
            if (data.unitPrice == null) {
                data.unitPrice = parseBigDecimal(fields, "referencePrice");
            }

            // 2. 解析数量
            data.quantity = parseBigDecimal(fields, "quantity");

            // 3. 解析其他字段
            if (fields.has("deliveryPlace")) {
                data.deliveryPlace = fields.get("deliveryPlace").asText();
            }
            if (fields.has("arrivalDate")) {
                String dateStr = fields.get("arrivalDate").asText();
                try {
                    data.arrivalDate = LocalDate.parse(dateStr);
                } catch (Exception ignored) {}
            }
            if (fields.has("paymentMethod")) {
                data.paymentMethod = fields.get("paymentMethod").asText();
            }
            if (fields.has("deliveryMethod")) {
                data.deliveryMode = fields.get("deliveryMethod").asText();
            }
            if (fields.has("basisPrice")) {
                data.basisPrice = parseBigDecimal(fields, "basisPrice");
            }
            if (fields.has("contractCode")) {
                data.contractCode = fields.get("contractCode").asText();
            }
            if (fields.has("dynamicParams")) {
                data.paramsJson = objectMapper.writeValueAsString(fields.get("dynamicParams"));
            }
        } catch (Exception e) {
            log.warn("parseQuotePayload failed: {}", e.getMessage());
        }
        return data;
    }

    private BigDecimal parseBigDecimal(JsonNode node, String fieldName) {
        if (!node.has(fieldName) || node.get(fieldName).isNull()) return null;
        try {
            String val = node.get(fieldName).asText().replaceAll("[^\\d.-]", "");
            if (val.isEmpty()) return null;
            return new BigDecimal(val);
        } catch (Exception e) {
            return null;
        }
    }

    private String generateContractNo() {
        String prefix = "CT";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + date + random;
    }

    private String generateDefaultTermsJson(BusContract c) {
        try {
            Map<String, Object> terms = new LinkedHashMap<>();
            terms.put("product", Map.of(
                "name", nvl(c.getProductName(), "____"),
                "quantity", c.getQuantity() != null ? c.getQuantity().toPlainString() : "____",
                "unit", nvl(c.getUnit(), "吨"),
                "unitPrice", c.getUnitPrice() != null ? c.getUnitPrice().toPlainString() : "____",
                "basisPrice", c.getBasisPrice() != null ? c.getBasisPrice().toPlainString() : "",
                "contractCode", nvl(c.getContractCode(), ""),
                "params", parseProductParams(c.getParamsJson())
            ));
            terms.put("delivery", Map.of(
                "address", nvl(c.getDeliveryAddress(), "____"),
                "date", c.getDeliveryDate() != null ? c.getDeliveryDate().toString() : "____",
                "mode", nvl(c.getDeliveryMode(), "____")
            ));
            terms.put("payment", nvl(c.getPaymentMethod(), "____"));
            terms.put("liability", "任何一方违反本合同约定的，应承担违约责任。");
            terms.put("dispute", "本合同发生争议，双方应友好协商解决；协商不成的，提交甲方所在地人民法院管辖。");
            return objectMapper.writeValueAsString(terms);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void logChange(Long contractId, String changeType, String changeDesc, String before, String after, Long operatorUserId) {
        BusContractChangeLog log = new BusContractChangeLog();
        log.setContractId(contractId);
        log.setChangeType(changeType);
        log.setChangeDesc(changeDesc);
        log.setBeforeJson(before);
        log.setAfterJson(after);
        log.setOperatorUserId(operatorUserId);
        changeLogMapper.insert(log);
    }

    private ContractResponse toResponse(BusContract c, Long viewerUserId) {
        ContractResponse o = new ContractResponse();
        o.setId(c.getId());
        o.setQuoteMessageId(c.getQuoteMessageId());
        o.setConversationId(c.getConversationId());
        o.setContractNo(c.getContractNo());
        o.setBuyerCompanyId(c.getBuyerCompanyId());
        o.setSellerCompanyId(c.getSellerCompanyId());
        
        // 获取公司名称
        if (c.getBuyerCompanyId() != null) {
            BusCompany buyer = companyMapper.selectById(c.getBuyerCompanyId());
            if (buyer != null) o.setBuyerCompanyName(buyer.getCompanyName());
        }
        if (c.getSellerCompanyId() != null) {
            BusCompany seller = companyMapper.selectById(c.getSellerCompanyId());
            if (seller != null) o.setSellerCompanyName(seller.getCompanyName());
        }
        
        o.setProductName(c.getProductName());
        o.setCategoryName(c.getCategoryName());
        o.setQuantity(c.getQuantity());
        o.setUnit(c.getUnit());
        o.setUnitPrice(c.getUnitPrice());
        o.setParamsJson(c.getParamsJson());
        o.setBasisPrice(c.getBasisPrice());
        o.setContractCode(c.getContractCode());
        o.setTotalAmount(c.getTotalAmount());
        o.setDeliveryDate(c.getDeliveryDate());
        o.setDeliveryAddress(c.getDeliveryAddress());
        o.setPaymentMethod(c.getPaymentMethod());
        o.setDeliveryMode(c.getDeliveryMode());
        o.setTermsJson(c.getTermsJson());
        o.setStatus(c.getStatus());
        o.setBuyerSignTime(c.getBuyerSignTime());
        o.setSellerSignTime(c.getSellerSignTime());
        o.setPdfHash(c.getPdfHash());
        o.setPdfUrl(c.getPdfUrl());
        o.setCreateTime(c.getCreateTime());
        o.setUpdateTime(c.getUpdateTime());
        
        // 查询签署状态
        BusContractSignature sigBuyer = signatureMapper.selectByContractAndParty(c.getId(), "buyer");
        BusContractSignature sigSeller = signatureMapper.selectByContractAndParty(c.getId(), "seller");
        o.setBuyerSigned(sigBuyer != null);
        o.setSellerSigned(sigSeller != null);
        
        return o;
    }

    private static BigDecimal calcTotal(BigDecimal qty, BigDecimal unitPrice) {
        if (qty == null || unitPrice == null) return null;
        return qty.multiply(unitPrice);
    }

    private static String emptyToNull(String s) {
        if (!StringUtils.hasText(s)) return null;
        return s.trim();
    }

    private static float writeBlock(PDPageContentStream cs, float x, float y, String line) throws Exception {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(x, y);
        cs.showText(safeAscii(line));
        cs.endText();
        return y - 14;
    }

    private static String safeAscii(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (char ch : s.toCharArray()) {
            out.append(ch <= 0x7E ? ch : ' ');
        }
        return out.toString().trim();
    }

    private static String nvl(String s, String d) {
        return (s == null || s.isBlank()) ? d : s;
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(bytes);
        try (Formatter f = new Formatter()) {
            for (byte b : dig) f.format("%02x", b);
            return f.toString();
        }
    }
    
    // ==================== Detail Response Helpers ====================
    
    /**
     * 将关联查询结果转换为详情响应（包含公司详细信息、产品参数、格式化条款）
     */
    private ContractResponse toDetailResponse(Map<String, Object> detail, Long viewerUserId) {
        ContractResponse o = new ContractResponse();
        
        // 基本信息
        o.setId(getLongValue(detail, "id"));
        o.setQuoteMessageId(getLongValue(detail, "quote_message_id"));
        o.setConversationId(getLongValue(detail, "conversation_id"));
        o.setContractNo(getStringValue(detail, "contract_no"));
        
        // 买方基本信息
        o.setBuyerCompanyId(getLongValue(detail, "buyer_company_id"));
        o.setBuyerCompanyName(getStringValue(detail, "buyer_company_name"));
        
        // 买方详细信息
        o.setBuyerLicenseNo(getStringValue(detail, "buyer_license_no"));
        o.setBuyerContacts(getStringValue(detail, "buyer_contacts"));
        o.setBuyerPhone(getStringValue(detail, "buyer_phone"));
        o.setBuyerAddress(getStringValue(detail, "buyer_address"));
        o.setBuyerBankInfo(getStringValue(detail, "buyer_bank_info"));
        
        // 卖方基本信息
        o.setSellerCompanyId(getLongValue(detail, "seller_company_id"));
        o.setSellerCompanyName(getStringValue(detail, "seller_company_name"));
        
        // 卖方详细信息
        o.setSellerLicenseNo(getStringValue(detail, "seller_license_no"));
        o.setSellerContacts(getStringValue(detail, "seller_contacts"));
        o.setSellerPhone(getStringValue(detail, "seller_phone"));
        o.setSellerAddress(getStringValue(detail, "seller_address"));
        o.setSellerBankInfo(getStringValue(detail, "seller_bank_info"));
        
        // 产品信息
        o.setProductName(getStringValue(detail, "product_name"));
        o.setCategoryName(getStringValue(detail, "category_name"));
        o.setQuantity(getBigDecimalValue(detail, "quantity"));
        o.setUnit(getStringValue(detail, "unit"));
        o.setUnitPrice(getBigDecimalValue(detail, "unit_price"));
        o.setParamsJson(getStringValue(detail, "params_json"));
        o.setTotalAmount(getBigDecimalValue(detail, "total_amount"));
        
        // 解析产品参数
        o.setProductParams(parseProductParams(o.getParamsJson()));
        
        // 交付信息
        o.setDeliveryDate(getLocalDateValue(detail, "delivery_date"));
        o.setDeliveryAddress(getStringValue(detail, "delivery_address"));
        o.setPaymentMethod(getStringValue(detail, "payment_method"));
        o.setDeliveryMode(getStringValue(detail, "delivery_mode"));
        o.setTermsJson(getStringValue(detail, "terms_json"));
        
        // 生成格式化条款
        o.setFormattedTerms(generateFormattedTerms(o));
        
        // 状态
        o.setStatus(getIntValue(detail, "status"));
        o.setBuyerSignTime(getLocalDateTimeValue(detail, "buyer_sign_time"));
        o.setSellerSignTime(getLocalDateTimeValue(detail, "seller_sign_time"));
        o.setPdfHash(getStringValue(detail, "pdf_hash"));
        o.setPdfUrl(getStringValue(detail, "pdf_url"));
        o.setCreateTime(getLocalDateTimeValue(detail, "create_time"));
        o.setUpdateTime(getLocalDateTimeValue(detail, "update_time"));
        
        // 查询签署状态
        Long contractId = o.getId();
        if (contractId != null) {
            BusContractSignature sigBuyer = signatureMapper.selectByContractAndParty(contractId, "buyer");
            BusContractSignature sigSeller = signatureMapper.selectByContractAndParty(contractId, "seller");
            o.setBuyerSigned(sigBuyer != null);
            o.setSellerSigned(sigSeller != null);
        }
        
        return o;
    }
    
    /**
     * 解析产品参数 JSON 为结构化列表
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseProductParams(String paramsJson) {
        if (!StringUtils.hasText(paramsJson)) return Collections.emptyList();
        
        // 黑名单：系统元数据、重复展示的业务字段
        Set<String> blacklist = Set.of(
            "snapshotTime", "priceType", "id", "categoryName", "title", 
            "productName", "companyName", "nickName", "exFactoryPrice", "expectedPrice",
            "remainingQuantity", "unit", "basisQuotes", "basisPrice", 
            "contractCode", "futuresPrice", "originPrice", "shipAddress", "purchaseAddress",
            "deliveryMode", "storageMethod", "packaging", "paramsJson"
        );

        // 字段名映射
        Map<String, String> fieldMap = Map.of(
            "origin", "产地",
            "quantity", "发布量",
            "remark", "备注",
            "storageMethod", "存储方式",
            "packaging", "包装方式",
            "deliveryMode", "交货方式",
            "productionDate", "生产日期",
            "shelfLife", "保质期"
        );
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(paramsJson);
            
            // 内部递归处理函数
            processNode(root, blacklist, fieldMap, result);
            
            return result;
        } catch (Exception e) {
            log.warn("parseProductParams failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private void processNode(JsonNode node, Set<String> blacklist, Map<String, String> fieldMap, List<Map<String, Object>> result) {
        if (node.isObject()) {
            // 如果是嵌套格式 {"params": {...}, "custom": {...}}，先展开
            if (node.has("params") || node.has("custom")) {
                if (node.has("params")) processNode(node.get("params"), blacklist, fieldMap, result);
                if (node.has("custom")) processNode(node.get("custom"), blacklist, fieldMap, result);
                return;
            }

            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode val = entry.getValue();

                if (blacklist.contains(key) || key.matches("\\d+")) return;
                
                // 过滤空值
                if (val.isNull() || val.asText().equals("null") || val.asText().isBlank()) return;

                // 智能解压 paramsJson 字符串
                if ("paramsJson".equals(key) && val.isTextual() && val.asText().startsWith("{")) {
                    try {
                        JsonNode inner = objectMapper.readTree(val.asText());
                        processNode(inner, blacklist, fieldMap, result);
                        return;
                    } catch (Exception ignored) {}
                }

                Map<String, Object> p = new LinkedHashMap<>();
                if (val.isObject() && val.has("name") && val.has("value")) {
                    p.put("label", val.get("name").asText());
                    p.put("value", val.get("value").asText());
                } else {
                    p.put("label", fieldMap.getOrDefault(key, key));
                    p.put("value", val.asText());
                }
                result.add(p);
            });
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                if (item.isObject()) {
                    Map<String, Object> p = new LinkedHashMap<>();
                    String label = null;
                    if (item.has("label")) label = item.get("label").asText();
                    else if (item.has("name")) label = item.get("name").asText();
                    
                    if (label != null && !blacklist.contains(label)) {
                        p.put("label", fieldMap.getOrDefault(label, label));
                        if (item.has("value")) p.put("value", item.get("value").asText());
                        if (!p.isEmpty()) result.add(p);
                    }
                }
            }
        }
    }
    
    /**
     * 生成格式化的合同条款文本
     */
    private String generateFormattedTerms(ContractResponse c) {
        StringBuilder sb = new StringBuilder();
        
        // 合同标题
        sb.append("【农产品采购合同】\n\n");
        sb.append("合同编号：").append(nvl(c.getContractNo(), "____")).append("\n");
        sb.append("签订日期：").append(c.getCreateTime() != null ? 
            c.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) : "____年__月__日").append("\n\n");
        
        // 第一条：合同主体
        sb.append("第一条  合同主体\n");
        sb.append("甲方（买方）：").append(nvl(c.getBuyerCompanyName(), "____")).append("\n");
        sb.append("统一社会信用代码：").append(nvl(c.getBuyerLicenseNo(), "____")).append("\n");
        sb.append("联系人：").append(nvl(c.getBuyerContacts(), "____")).append("  电话：").append(nvl(c.getBuyerPhone(), "____")).append("\n");
        sb.append("地址：").append(nvl(c.getBuyerAddress(), "____")).append("\n\n");
        
        sb.append("乙方（卖方）：").append(nvl(c.getSellerCompanyName(), "____")).append("\n");
        sb.append("统一社会信用代码：").append(nvl(c.getSellerLicenseNo(), "____")).append("\n");
        sb.append("联系人：").append(nvl(c.getSellerContacts(), "____")).append("  电话：").append(nvl(c.getSellerPhone(), "____")).append("\n");
        sb.append("地址：").append(nvl(c.getSellerAddress(), "____")).append("\n\n");
        
        // 第二条：标的物
        sb.append("第二条  标的物\n");
        sb.append("产品名称：").append(nvl(c.getProductName(), "____")).append("\n");
        sb.append("产品类目：").append(nvl(c.getCategoryName(), "____")).append("\n");
        sb.append("数量：").append(c.getQuantity() != null ? c.getQuantity().toPlainString() : "____");
        sb.append(" ").append(nvl(c.getUnit(), "吨")).append("\n");
        
        if (c.getBasisPrice() != null && c.getContractCode() != null) {
            sb.append("定价方式：基差定价\n");
            sb.append("挂钩合约：").append(c.getContractCode()).append("\n");
            sb.append("基差：").append(c.getBasisPrice().compareTo(BigDecimal.ZERO) > 0 ? "+" : "").append(c.getBasisPrice().toPlainString()).append(" 元/吨\n");
            sb.append("最终结算单价 = 签署合同时点的期货盘面价 + 基差\n");
            sb.append("总金额：待结算（签署合同时根据盘面核算）\n\n");
        } else {
            sb.append("单价：人民币 ").append(c.getUnitPrice() != null ? c.getUnitPrice().toPlainString() : "____");
            sb.append(" 元/").append(nvl(c.getUnit(), "吨")).append("\n");
            sb.append("总金额：人民币 ").append(c.getTotalAmount() != null ? c.getTotalAmount().toPlainString() : "____").append(" 元\n\n");
        }
        
        // 质量标准
        sb.append("第三条  质量标准\n");
        sb.append("产品质量符合国家相关标准及双方约定的技术指标：\n");
        List<Map<String, Object>> params = c.getProductParams();
        if (params != null && !params.isEmpty()) {
            for (Map<String, Object> param : params) {
                sb.append("  • ").append(param.get("label")).append("：").append(param.get("value")).append("\n");
            }
        } else {
            sb.append("  （按国家标准执行）\n");
        }
        sb.append("\n");
        
        // 第四条：交付条款
        sb.append("第四条  交付条款\n");
        sb.append("交付地点：").append(nvl(c.getDeliveryAddress(), "____")).append("\n");
        sb.append("交付日期：").append(c.getDeliveryDate() != null ? 
            c.getDeliveryDate().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) : "____年__月__日").append("\n");
        sb.append("交付方式：").append(nvl(c.getDeliveryMode(), "____")).append("\n");
        sb.append("运输及风险：货物在交付前的风险由卖方承担，交付后由买方承担。\n\n");
        
        // 第五条：付款条款
        sb.append("第五条  付款条款\n");
        sb.append("付款方式：").append(getPaymentMethodText(c.getPaymentMethod())).append("\n");
        sb.append("发票类型：增值税专用发票\n\n");
        
        // 第六条：验收条款
        sb.append("第六条  验收条款\n");
        sb.append("1. 买方应在收货后 3 个工作日内完成验收。\n");
        sb.append("2. 如发现质量问题，应在验收期内书面通知卖方，逾期视为验收合格。\n");
        sb.append("3. 验收以到货检验结果为准，双方对检验结果有异议的，可委托第三方检测机构进行仲裁检测。\n\n");
        
        // 第七条：违约责任
        sb.append("第七条  违约责任\n");
        sb.append("1. 卖方逾期交货的，每逾期一日，按合同总金额的 0.05% 向买方支付违约金。\n");
        sb.append("2. 买方逾期付款的，每逾期一日，按未付金额的 0.05% 向卖方支付违约金。\n");
        sb.append("3. 卖方交付的产品质量不符合约定的，买方有权要求换货、退货或相应减少价款。\n\n");
        
        // 第八条：争议解决
        sb.append("第八条  争议解决\n");
        sb.append("本合同发生争议，双方应友好协商解决；协商不成的，任何一方均可向甲方（买方）所在地人民法院提起诉讼。\n\n");
        
        // 第九条：不可抗力
        sb.append("第九条  不可抗力\n");
        sb.append("因地震、台风、洪水、战争等不可抗力因素导致合同无法履行的，受影响方应及时通知对方，并在合理期限内提供相关证明。双方可协商解除合同或延期履行，均不承担违约责任。\n\n");
        
        // 第十条：其他条款
        sb.append("第十条  其他条款\n");
        sb.append("1. 本合同未尽事宜，双方可另行协商并签订补充协议。\n");
        sb.append("2. 本合同一式贰份，甲乙双方各执壹份，具有同等法律效力。\n");
        sb.append("3. 本合同自双方签字（或盖章）之日起生效。\n\n");
        
        // 签署区
        sb.append("【签署区】\n\n");
        sb.append("甲方（盖章）：____________________    乙方（盖章）：____________________\n\n");
        sb.append("法定代表人（或授权代表）：________    法定代表人（或授权代表）：________\n\n");
        sb.append("签署日期：____年__月__日            签署日期：____年__月__日\n");
        
        return sb.toString();
    }
    
    /**
     * 将付款方式代码转换为文本
     */
    private String getPaymentMethodText(String code) {
        if (code == null) return "____";
        return switch (code) {
            case "01", "款到发货" -> "款到发货";
            case "02", "货到付款" -> "货到付款";
            case "03", "账期30天" -> "账期30天（货到后30天内付款）";
            case "04", "账期60天" -> "账期60天（货到后60天内付款）";
            case "05", "分期付款" -> "分期付款（按约定分期支付）";
            case "06", "预付定金" -> "预付定金（签约时支付30%定金，交货时付清余款）";
            default -> code;
        };
    }
    
    // ==================== Map Value Helpers ====================
    
    private Long getLongValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Long l) return l;
        if (val instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Integer i) return i;
        if (val instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getStringValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        return val.toString();
    }
    
    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof BigDecimal bd) return bd;
        if (val instanceof Number n) return new BigDecimal(n.toString());
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof LocalDate ld) return ld;
        if (val instanceof java.sql.Date d) return d.toLocalDate();
        try {
            return LocalDate.parse(val.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof LocalDateTime ldt) return ldt;
        if (val instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        try {
            return LocalDateTime.parse(val.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
