package com.agrimatch.chat.event;

import org.springframework.context.ApplicationEvent;

/**
 * 消息更新事件，用于触发 WebSocket 广播消息更新通知
 */
public class MessageUpdateEvent extends ApplicationEvent {
    private final Long messageId;
    private final Long conversationId;
    private final Long aUserId;
    private final Long bUserId;
    private final String payloadJson;

    public MessageUpdateEvent(Object source, Long messageId, Long conversationId, 
                              Long aUserId, Long bUserId, String payloadJson) {
        super(source);
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.aUserId = aUserId;
        this.bUserId = bUserId;
        this.payloadJson = payloadJson;
    }

    public Long getMessageId() { return messageId; }
    public Long getConversationId() { return conversationId; }
    public Long getAUserId() { return aUserId; }
    public Long getBUserId() { return bUserId; }
    public String getPayloadJson() { return payloadJson; }
}

