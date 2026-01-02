package com.agrimatch.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatConversationOpenRequest {
    @NotNull
    private Long peerUserId;

    @NotBlank
    private String subjectType; // SUPPLY / NEED

    @NotNull
    private Long subjectId;

    // optional: 标的快照 JSON（用于聊天置顶卡片）
    private String subjectSnapshotJson;

    public Long getPeerUserId() {
        return peerUserId;
    }

    public void setPeerUserId(Long peerUserId) {
        this.peerUserId = peerUserId;
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
}



