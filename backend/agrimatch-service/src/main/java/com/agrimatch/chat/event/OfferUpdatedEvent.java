package com.agrimatch.chat.event;

import com.agrimatch.chat.dto.ChatMessageResponse;
import org.springframework.context.ApplicationEvent;

public class OfferUpdatedEvent extends ApplicationEvent {
    private final Long conversationId;
    private final Long aUserId;
    private final Long bUserId;
    private final ChatMessageResponse message;

    public OfferUpdatedEvent(Object source, Long conversationId, Long aUserId, Long bUserId, ChatMessageResponse message) {
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
