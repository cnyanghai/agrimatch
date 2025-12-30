package com.agrimatch.chat.service;

import com.agrimatch.chat.dto.ChatMessageResponse;
import com.agrimatch.chat.dto.ChatPeerResponse;

import java.util.List;

public interface ChatService {
    Long send(Long fromUserId, Long toUserId, String content);

    List<ChatMessageResponse> history(Long userId, Long peerUserId, Integer limit);

    List<ChatPeerResponse> peers(Long userId);

    void markRead(Long userId, Long peerUserId);
}


