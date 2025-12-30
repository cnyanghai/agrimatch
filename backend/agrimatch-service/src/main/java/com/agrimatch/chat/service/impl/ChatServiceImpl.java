package com.agrimatch.chat.service.impl;

import com.agrimatch.chat.domain.BusChatMessage;
import com.agrimatch.chat.dto.ChatMessageResponse;
import com.agrimatch.chat.dto.ChatPeerResponse;
import com.agrimatch.chat.mapper.ChatMapper;
import com.agrimatch.chat.service.ChatService;
import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatMapper chatMapper;

    public ChatServiceImpl(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
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
            r.setFromUserId(m.getFromUserId());
            r.setToUserId(m.getToUserId());
            r.setFromUserName(m.getFromUserName());
            r.setFromNickName(m.getFromNickName());
            r.setToUserName(m.getToUserName());
            r.setToNickName(m.getToNickName());
            r.setContent(m.getContent());
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
}


