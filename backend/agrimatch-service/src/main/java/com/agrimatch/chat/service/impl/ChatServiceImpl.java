package com.agrimatch.chat.service.impl;

import com.agrimatch.chat.domain.BusChatConversation;
import com.agrimatch.chat.domain.BusChatMessage;
import com.agrimatch.chat.dto.ChatConversationResponse;
import com.agrimatch.chat.dto.ChatMessageResponse;
import com.agrimatch.chat.dto.ChatPeerResponse;
import com.agrimatch.chat.event.OfferUpdatedEvent;
import com.agrimatch.chat.mapper.ChatMapper;
import com.agrimatch.chat.service.ChatService;
import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatMapper chatMapper;
    private final ApplicationEventPublisher eventPublisher;

    public ChatServiceImpl(ChatMapper chatMapper, ApplicationEventPublisher eventPublisher) {
        this.chatMapper = chatMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Long send(Long fromUserId, Long toUserId, String content) {
        if (fromUserId == null) throw new ApiException(401, "未登录");
        if (toUserId == null || !StringUtils.hasText(content)) throw new ApiException(ResultCode.PARAM_ERROR);
        if (content.length() > 2000) throw new ApiException(400, "消息过长");

        BusChatMessage m = new BusChatMessage();
        m.setFromUserId(fromUserId);
        m.setToUserId(toUserId);
        m.setMsgType("TEXT");
        m.setContent(content.trim());
        int rows = chatMapper.insertMessage(m);
        if (rows != 1 || m.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);
        return m.getId();
    }

    @Override
    public List<ChatMessageResponse> history(Long userId, Long peerUserId, Integer limit) {
        if (userId == null || peerUserId == null) throw new ApiException(ResultCode.PARAM_ERROR);
        int lim = (limit == null ? 50 : Math.max(1, Math.min(limit, 200)));
        List<BusChatMessage> list = chatMapper.selectHistory(userId, peerUserId, lim);
        // 当前是倒序，前端体验更好：翻转为正序
        Collections.reverse(list);
        List<ChatMessageResponse> out = new ArrayList<>();
        for (BusChatMessage m : list) {
            ChatMessageResponse r = new ChatMessageResponse();
            r.setId(m.getId());
            r.setConversationId(m.getConversationId());
            r.setFromUserId(m.getFromUserId());
            r.setToUserId(m.getToUserId());
            r.setFromUserName(m.getFromUserName());
            r.setFromNickName(m.getFromNickName());
            r.setToUserName(m.getToUserName());
            r.setToNickName(m.getToNickName());
            r.setMsgType(m.getMsgType());
            r.setContent(m.getContent());
            r.setPayloadJson(m.getPayloadJson());
            r.setQuoteStatus(m.getQuoteStatus());
            r.setBasisPrice(m.getBasisPrice());
            r.setContractCode(m.getContractCode());
            r.setRead(m.getIsRead() != null && m.getIsRead() == 1);
            r.setCreateTime(m.getCreateTime());
            out.add(r);
        }
        return out;
    }

    @Override
    public List<ChatPeerResponse> peers(Long userId) {
        if (userId == null) throw new ApiException(401, "未登录");
        List<ChatMapper.PeerRow> rows = chatMapper.selectPeers(userId);
        List<ChatPeerResponse> out = new ArrayList<>();
        for (ChatMapper.PeerRow r : rows) {
            ChatPeerResponse o = new ChatPeerResponse();
            o.setPeerUserId(r.getPeerUserId());
            o.setPeerUserName(r.getPeerUserName());
            o.setPeerNickName(r.getPeerNickName());
            o.setPeerCompanyName(r.getPeerCompanyName());
            o.setLastContent(r.getLastContent());
            o.setLastTime(r.getLastTime());
            o.setUnreadCount(r.getUnreadCount());
            out.add(o);
        }
        return out;
    }

    @Override
    public void markRead(Long userId, Long peerUserId) {
        if (userId == null || peerUserId == null) throw new ApiException(ResultCode.PARAM_ERROR);
        chatMapper.markReadFromPeer(userId, peerUserId);
    }

    @Override
    @Transactional
    public Long openConversation(Long userId, Long peerUserId, String subjectType, Long subjectId, String subjectSnapshotJson) {
        if (userId == null) throw new ApiException(401, "未登录");
        if (peerUserId == null || subjectId == null || !StringUtils.hasText(subjectType)) throw new ApiException(ResultCode.PARAM_ERROR);
        String st = subjectType.trim().toUpperCase();
        if (!"SUPPLY".equals(st) && !"NEED".equals(st)) throw new ApiException(400, "subjectType 仅支持 SUPPLY/NEED");
        if (userId.equals(peerUserId)) throw new ApiException(400, "不能与自己发起会话");

        long a = Math.min(userId, peerUserId);
        long b = Math.max(userId, peerUserId);

        Long existing = chatMapper.selectConversationId(a, b, st, subjectId);
        if (existing != null) {
            if (StringUtils.hasText(subjectSnapshotJson)) {
                chatMapper.updateConversationSnapshot(existing, subjectSnapshotJson);
            }
            return existing;
        }

        BusChatConversation c = new BusChatConversation();
        c.setAUserId(a);
        c.setBUserId(b);
        c.setSubjectType(st);
        c.setSubjectId(subjectId);
        c.setSubjectSnapshotJson(StringUtils.hasText(subjectSnapshotJson) ? subjectSnapshotJson : null);
        int rows = chatMapper.insertConversation(c);
        if (rows != 1 || c.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);
        return c.getId();
    }

    @Override
    public List<ChatConversationResponse> conversations(Long userId) {
        if (userId == null) throw new ApiException(401, "未登录");
        List<ChatMapper.ConversationRow> rows = chatMapper.selectConversations(userId);
        List<ChatConversationResponse> out = new ArrayList<>();
        for (ChatMapper.ConversationRow r : rows) {
            ChatConversationResponse o = new ChatConversationResponse();
            o.setId(r.getId());
            o.setPeerUserId(r.getPeerUserId());
            o.setPeerUserName(r.getPeerUserName());
            o.setPeerNickName(r.getPeerNickName());
            o.setPeerCompanyName(r.getPeerCompanyName());
            o.setSubjectType(r.getSubjectType());
            o.setSubjectId(r.getSubjectId());
            o.setSubjectSnapshotJson(r.getSubjectSnapshotJson());
            o.setLastContent(r.getLastContent());
            o.setLastTime(r.getLastTime());
            o.setUnreadCount(r.getUnreadCount());
            out.add(o);
        }
        return out;
    }

    @Override
    public List<ChatMessageResponse> conversationMessages(Long userId, Long conversationId, Integer limit) {
        requireConversationMember(userId, conversationId);
        int lim = (limit == null ? 50 : Math.max(1, Math.min(limit, 200)));
        List<BusChatMessage> list = chatMapper.selectConversationMessages(conversationId, lim);
        Collections.reverse(list);
        List<ChatMessageResponse> out = new ArrayList<>();
        for (BusChatMessage m : list) {
            ChatMessageResponse r = new ChatMessageResponse();
            r.setId(m.getId());
            r.setConversationId(m.getConversationId());
            r.setFromUserId(m.getFromUserId());
            r.setToUserId(m.getToUserId());
            r.setFromUserName(m.getFromUserName());
            r.setFromNickName(m.getFromNickName());
            r.setToUserName(m.getToUserName());
            r.setToNickName(m.getToNickName());
            r.setMsgType(m.getMsgType());
            r.setContent(m.getContent());
            r.setPayloadJson(m.getPayloadJson());
            r.setQuoteStatus(m.getQuoteStatus());
            r.setBasisPrice(m.getBasisPrice());
            r.setContractCode(m.getContractCode());
            r.setRead(m.getIsRead() != null && m.getIsRead() == 1);
            r.setCreateTime(m.getCreateTime());
            out.add(r);
        }
        return out;
    }

    @Override
    public void markConversationRead(Long userId, Long conversationId) {
        requireConversationMember(userId, conversationId);
        chatMapper.markReadInConversation(conversationId, userId);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendToConversation(Long fromUserId, Long conversationId, String msgType, String content, String payloadJson, java.math.BigDecimal basisPrice, String contractCode) {
        ChatMapper.ConversationUserPair pair = requireConversationMember(fromUserId, conversationId);
        long toUserId = fromUserId.equals(pair.getAUserId()) ? pair.getBUserId() : pair.getAUserId();

        String mt = (StringUtils.hasText(msgType) ? msgType.trim().toUpperCase() : "TEXT");
        if (!"TEXT".equals(mt) && !"QUOTE".equals(mt) && !"SYSTEM".equals(mt) && !"ATTACHMENT".equals(mt) && !"IMAGE".equals(mt) && !"CONTRACT".equals(mt)) {
            throw new ApiException(400, "msgType 不支持");
        }
        if ("TEXT".equals(mt) && !StringUtils.hasText(content)) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        String safeContent = StringUtils.hasText(content) ? content.trim() : "";
        if (safeContent.length() > 2000) throw new ApiException(400, "消息过长");

        if ("QUOTE".equals(mt)) {
            // 新报价发出，将会话中旧的待确认报价置为失效
            chatMapper.expireOldQuotes(conversationId, null);
        }

        BusChatMessage m = new BusChatMessage();
        m.setConversationId(conversationId);
        m.setFromUserId(fromUserId);
        m.setToUserId(toUserId);
        m.setMsgType(mt);
        m.setContent(safeContent);
        m.setPayloadJson(StringUtils.hasText(payloadJson) ? payloadJson : null);
        m.setBasisPrice(basisPrice);
        m.setContractCode(contractCode);
        if ("QUOTE".equals(mt)) {
            m.setQuoteStatus("OFFERED");
        }

        int rows = chatMapper.insertMessage(m);
        if (rows != 1 || m.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);

        String lastContent = safeContent;
        if (!"TEXT".equals(mt) && !StringUtils.hasText(lastContent)) {
            if ("QUOTE".equals(mt)) lastContent = "[报价]";
            else if ("IMAGE".equals(mt)) lastContent = "[图片]";
            else if ("ATTACHMENT".equals(mt)) lastContent = "[附件]";
            else lastContent = "[系统]";
        }
        chatMapper.updateConversationLast(conversationId, m.getId(), lastContent);

        ChatMessageResponse r = new ChatMessageResponse();
        r.setId(m.getId());
        r.setConversationId(conversationId);
        r.setFromUserId(fromUserId);
        r.setToUserId(toUserId);
        r.setMsgType(mt);
        r.setContent(safeContent);
        r.setPayloadJson(m.getPayloadJson());
        r.setBasisPrice(m.getBasisPrice());
        r.setContractCode(m.getContractCode());
        r.setQuoteStatus(m.getQuoteStatus());
        r.setRead(false);
        r.setCreateTime(LocalDateTime.now());
        return r;
    }

    @Override
    @Transactional
    public ChatMessageResponse confirmOffer(Long userId, Long messageId) {
        if (userId == null || messageId == null) throw new ApiException(ResultCode.PARAM_ERROR);
        BusChatMessage m = chatMapper.selectMessageById(messageId);
        if (m == null || m.getIsDeleted() == 1) throw new ApiException(ResultCode.NOT_FOUND);

        // 只有报价卡可以被确认
        if (!"QUOTE".equals(m.getMsgType())) throw new ApiException(400, "只有报价消息可以被确认");
        // 只有接收方可以确认
        if (!userId.equals(m.getToUserId())) throw new ApiException(403, "只有接收方可以确认报价");
        // 只有 OFFERED 状态可以被确认
        if (!"OFFERED".equals(m.getQuoteStatus())) throw new ApiException(400, "该报价已生效或已失效");

        // 更新当前报价状态
        chatMapper.updateQuoteStatus(messageId, "ACCEPTED");
        // 会话中其他待确认报价置为失效
        chatMapper.expireOldQuotes(m.getConversationId(), messageId);

        // 插入系统消息通知
        sendToConversation(m.getFromUserId(), m.getConversationId(), "SYSTEM", "对方已确认您的报价，交易达成！", null, null, null);

        // 返回更新后的消息
        BusChatMessage updated = chatMapper.selectMessageById(messageId);
        ChatMessageResponse r = new ChatMessageResponse();
        r.setId(updated.getId());
        r.setConversationId(updated.getConversationId());
        r.setFromUserId(updated.getFromUserId());
        r.setToUserId(updated.getToUserId());
        r.setMsgType(updated.getMsgType());
        r.setContent(updated.getContent());
        r.setPayloadJson(updated.getPayloadJson());
        r.setQuoteStatus(updated.getQuoteStatus());
        r.setBasisPrice(updated.getBasisPrice());
        r.setContractCode(updated.getContractCode());
        r.setRead(updated.getIsRead() != null && updated.getIsRead() == 1);
        r.setCreateTime(updated.getCreateTime());

        // 发布事件通知 WebSocket 广播
        ChatMapper.ConversationUserPair pair = requireConversationMember(userId, m.getConversationId());
        eventPublisher.publishEvent(new OfferUpdatedEvent(this, m.getConversationId(), pair.getAUserId(), pair.getBUserId(), r));

        return r;
    }

    private ChatMapper.ConversationUserPair requireConversationMember(Long userId, Long conversationId) {
        if (userId == null) throw new ApiException(401, "未登录");
        if (conversationId == null) throw new ApiException(ResultCode.PARAM_ERROR);
        ChatMapper.ConversationUserPair pair = chatMapper.selectConversationUserPair(conversationId);
        if (pair == null || pair.getAUserId() == null || pair.getBUserId() == null) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        if (!userId.equals(pair.getAUserId()) && !userId.equals(pair.getBUserId())) {
            throw new ApiException(403, "无权访问该会话");
        }
        return pair;
    }
}


