package com.agrimatch.chat.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息已读事件 - 用于通知发送方其消息已被阅读
 */
public class MessagesReadEvent extends ApplicationEvent {
    private final Long conversationId;
    private final Long readByUserId;      // 阅读者ID
    private final Long notifyUserId;      // 需要通知的用户ID（消息发送者）
    private final List<Long> messageIds;  // 被标记为已读的消息ID列表
    private final LocalDateTime readAt;   // 阅读时间

    public MessagesReadEvent(Object source, Long conversationId, Long readByUserId, Long notifyUserId, List<Long> messageIds) {
        super(source);
        this.conversationId = conversationId;
        this.readByUserId = readByUserId;
        this.notifyUserId = notifyUserId;
        this.messageIds = messageIds;
        this.readAt = LocalDateTime.now();
    }

    public Long getConversationId() {
        return conversationId;
    }

    public Long getReadByUserId() {
        return readByUserId;
    }

    public Long getNotifyUserId() {
        return notifyUserId;
    }

    public List<Long> getMessageIds() {
        return messageIds;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }
}
