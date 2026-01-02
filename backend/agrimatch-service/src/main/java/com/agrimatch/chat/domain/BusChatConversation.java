package com.agrimatch.chat.domain;

import java.time.LocalDateTime;

public class BusChatConversation {
    private Long id;
    private Long aUserId;
    private Long bUserId;
    private String subjectType;
    private Long subjectId;
    private String subjectSnapshotJson;
    private Long lastMsgId;
    private String lastContent;
    private LocalDateTime lastTime;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAUserId() {
        return aUserId;
    }

    public void setAUserId(Long aUserId) {
        this.aUserId = aUserId;
    }

    public Long getBUserId() {
        return bUserId;
    }

    public void setBUserId(Long bUserId) {
        this.bUserId = bUserId;
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

    public Long getLastMsgId() {
        return lastMsgId;
    }

    public void setLastMsgId(Long lastMsgId) {
        this.lastMsgId = lastMsgId;
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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}



