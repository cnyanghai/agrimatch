package com.agrimatch.chat.event;

import com.agrimatch.chat.dto.ChatMessageResponse;
import org.springframework.context.ApplicationEvent;

/**
 * 合同消息创建事件，用于触发 WebSocket 广播
 */
public class ContractMessageEvent extends ApplicationEvent {
    private final Long conversationId;
    private final Long aUserId;
    private final Long bUserId;
    private final ChatMessageResponse message;

    public ContractMessageEvent(Object source, Long conversationId, Long aUserId, Long bUserId, ChatMessageResponse message) {
        super(source);
        this.conversationId = conversationId;
        this.aUserId = aUserId;
        this.bUserId = bUserId;
        this.message = message;
    }

    public Long getConversationId() { return conversationId; }
    public Long getAUserId() { return aUserId; }
    public Long getBUserId() { return bUserId; }
    public ChatMessageResponse getMessage() { return message; }
}

