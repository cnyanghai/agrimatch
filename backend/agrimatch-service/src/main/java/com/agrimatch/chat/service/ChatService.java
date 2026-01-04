package com.agrimatch.chat.service;

import com.agrimatch.chat.dto.ChatMessageResponse;
import com.agrimatch.chat.dto.ChatConversationResponse;
import com.agrimatch.chat.dto.ChatPeerResponse;

import java.util.List;

public interface ChatService {
    Long send(Long fromUserId, Long toUserId, String content);

    List<ChatMessageResponse> history(Long userId, Long peerUserId, Integer limit);

    List<ChatPeerResponse> peers(Long userId);

    void markRead(Long userId, Long peerUserId);

    Long openConversation(Long userId, Long peerUserId, String subjectType, Long subjectId, String subjectSnapshotJson);

    List<ChatConversationResponse> conversations(Long userId);

    List<ChatMessageResponse> conversationMessages(Long userId, Long conversationId, Integer limit);

    void markConversationRead(Long userId, Long conversationId);

    ChatMessageResponse sendToConversation(Long fromUserId, Long conversationId, String msgType, String content, String payloadJson);

    ChatMessageResponse confirmOffer(Long userId, Long messageId);
}


