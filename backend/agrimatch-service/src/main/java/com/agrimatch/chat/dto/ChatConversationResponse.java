package com.agrimatch.chat.dto;

import java.time.LocalDateTime;

public class ChatConversationResponse {
    private Long id;
    private Long peerUserId;
    private String peerUserName;
    private String peerNickName;
    private String peerCompanyName;

    private String subjectType;
    private Long subjectId;
    private String subjectSnapshotJson;

    private String lastContent;
    private LocalDateTime lastTime;
    private Integer unreadCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectSnapshotJson() {
        return subjectSnapshotJson;
    }

    public void setSubjectSnapshotJson(String subjectSnapshotJson) {
        this.subjectSnapshotJson = subjectSnapshotJson;
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



