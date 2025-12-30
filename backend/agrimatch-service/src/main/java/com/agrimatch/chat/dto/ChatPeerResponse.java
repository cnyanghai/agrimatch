package com.agrimatch.chat.dto;

import java.time.LocalDateTime;

public class ChatPeerResponse {
    private Long peerUserId;
    private String peerUserName;
    private String peerNickName;
    private String peerCompanyName;
    private String lastContent;
    private LocalDateTime lastTime;
    private Integer unreadCount;

    public Long getPeerUserId() {
        return peerUserId;
    }

    public void setPeerUserId(Long peerUserId) {
        this.peerUserId = peerUserId;
    }

    public String getPeerUserName() {
        return peerUserName;
    }

    public void setPeerUserName(String peerUserName) {
        this.peerUserName = peerUserName;
    }

    public String getPeerNickName() {
        return peerNickName;
    }

    public void setPeerNickName(String peerNickName) {
        this.peerNickName = peerNickName;
    }

    public String getPeerCompanyName() {
        return peerCompanyName;
    }

    public void setPeerCompanyName(String peerCompanyName) {
        this.peerCompanyName = peerCompanyName;
    }

    public String getLastContent() {
        return lastContent;
    }

    public void setLastContent(String lastContent) {
        this.lastContent = lastContent;
    }

    public LocalDateTime getLastTime() {
        return lastTime;
    }

    public void setLastTime(LocalDateTime lastTime) {
        this.lastTime = lastTime;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}


