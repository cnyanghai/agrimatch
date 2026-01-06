package com.agrimatch.contract.service.impl;

import com.agrimatch.chat.domain.BusChatConversation;
import com.agrimatch.chat.domain.BusChatMessage;
import com.agrimatch.chat.dto.ChatMessageResponse;
import com.agrimatch.chat.event.ContractMessageEvent;
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
        
        // 从会话标的快照中获取产品名称和分类名称
        BusChatConversation conversation = chatMapper.selectConversationById(quoteMsg.getConversationId());
        if (conversation != null && conversation.getSubjectSnapshotJson() != null) {
            try {
                JsonNode snapshot = objectMapper.readTree(conversation.getSubjectSnapshotJson());
                if (quoteData.productName == null && snapshot.has("productName")) {
                    quoteData.productName = snapshot.get("productName").asText();
                }
                if (quoteData.productName == null && snapshot.has("categoryName")) {
                    quoteData.productName = snapshot.get("categoryName").asText();
                }
                if (quoteData.categoryName == null && snapshot.has("categoryName")) {
                    quoteData.categoryName = snapshot.get("categoryName").asText();
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

        // 确定买卖双方（假设：接收报价并确认的一方是买方）
        Long buyerCompanyId = toCompanyId != null ? toCompanyId : currentUser.getCompanyId();
        Long sellerCompanyId = fromCompanyId;

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
        BusContract c = contractMapper.selectById(id);
        if (c == null) throw new ApiException(ResultCode.NOT_FOUND);
        
        // 检查权限
        SysUser viewer = userMapper.selectById(viewerUserId);
        if (viewer == null) throw new ApiException(401, "未登录");
        
        Long viewerCompanyId = viewer.getCompanyId();
        if (viewerCompanyId == null || 
            (!viewerCompanyId.equals(c.getBuyerCompanyId()) && !viewerCompanyId.equals(c.getSellerCompanyId()))) {
            throw new ApiException(403, "无权查看此合同");
        }
        
        return toResponse(c, viewerUserId);
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
        checkAndUpdateSignStatus(contractId);
    }

    @Override
    @Transactional
    public void checkAndUpdateSignStatus(Long contractId) {
        int signCount = signatureMapper.countByContractId(contractId);
        if (signCount >= 2) {
            // 双方都已签署，更新状态为已签署
            contractMapper.updateStatus(contractId, 2); // 2 = 已签署
            logChange(contractId, "STATUS", "双方签署完成，合同生效", "1", "2", null);
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
    }

    private QuoteData parseQuotePayload(String payloadJson) {
        QuoteData data = new QuoteData();
        if (!StringUtils.hasText(payloadJson)) return data;

        try {
            JsonNode root = objectMapper.readTree(payloadJson);
            JsonNode fields = root.has("fields") ? root.get("fields") : root;

            if (fields.has("price")) {
                String priceStr = fields.get("price").asText();
                try {
                    data.unitPrice = new BigDecimal(priceStr.replaceAll("[^\\d.]", ""));
                } catch (Exception ignored) {}
            }
            if (fields.has("quantity")) {
                String qtyStr = fields.get("quantity").asText();
                try {
                    data.quantity = new BigDecimal(qtyStr.replaceAll("[^\\d.]", ""));
                } catch (Exception ignored) {}
            }
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
            if (fields.has("dynamicParams")) {
                data.paramsJson = objectMapper.writeValueAsString(fields.get("dynamicParams"));
            }
        } catch (Exception e) {
            // 解析失败，返回空数据
        }
        return data;
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
                "unitPrice", c.getUnitPrice() != null ? c.getUnitPrice().toPlainString() : "____"
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
}
