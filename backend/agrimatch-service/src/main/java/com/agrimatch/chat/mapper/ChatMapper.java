package com.agrimatch.chat.mapper;

import com.agrimatch.chat.domain.BusChatConversation;
import com.agrimatch.chat.domain.BusChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMapper {
    int insertMessage(BusChatMessage m);
    
    BusChatMessage selectMessageById(@Param("id") Long id);

    List<BusChatMessage> selectHistory(@Param("a") Long a, @Param("b") Long b, @Param("limit") Integer limit);

    int markReadFromPeer(@Param("toUserId") Long toUserId, @Param("fromUserId") Long fromUserId);

    int updateQuoteStatus(@Param("id") Long id, @Param("status") String status);

    int expireOldQuotes(@Param("conversationId") Long conversationId, @Param("exceptId") Long exceptId);

    List<PeerRow> selectPeers(@Param("userId") Long userId);

    Long selectConversationId(@Param("aUserId") Long aUserId,
                              @Param("bUserId") Long bUserId,
                              @Param("subjectType") String subjectType,
                              @Param("subjectId") Long subjectId);

    int insertConversation(BusChatConversation c);
    
    BusChatConversation selectConversationById(@Param("id") Long id);

    int updateConversationSnapshot(@Param("id") Long id, @Param("subjectSnapshotJson") String subjectSnapshotJson);

    int updateConversationLast(@Param("id") Long id,
                               @Param("lastMsgId") Long lastMsgId,
                               @Param("lastContent") String lastContent);

    List<ConversationRow> selectConversations(@Param("userId") Long userId);

    List<BusChatMessage> selectConversationMessages(@Param("conversationId") Long conversationId,
                                                    @Param("limit") Integer limit);

    int markReadInConversation(@Param("conversationId") Long conversationId, @Param("toUserId") Long toUserId);

    ConversationUserPair selectConversationUserPair(@Param("conversationId") Long conversationId);

    class PeerRow {
        private Long peerUserId;
        private String peerUserName;
        private String peerNickName;
        private String peerCompanyName;
        private String lastContent;
        private java.time.LocalDateTime lastTime;
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

        public java.time.LocalDateTime getLastTime() {
            return lastTime;
        }

        public void setLastTime(java.time.LocalDateTime lastTime) {
            this.lastTime = lastTime;
        }

        public Integer getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(Integer unreadCount) {
            this.unreadCount = unreadCount;
        }
    }

    class ConversationRow {
        private Long id;
        private Long peerUserId;
        private String peerUserName;
        private String peerNickName;
        private String peerCompanyName;
        private String subjectType;
        private Long subjectId;
        private String subjectSnapshotJson;
        private String lastContent;
        private java.time.LocalDateTime lastTime;
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

        public java.time.LocalDateTime getLastTime() {
            return lastTime;
        }

        public void setLastTime(java.time.LocalDateTime lastTime) {
            this.lastTime = lastTime;
        }

        public Integer getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(Integer unreadCount) {
            this.unreadCount = unreadCount;
        }
    }

    class ConversationUserPair {
        private Long aUserId;
        private Long bUserId;

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
    }
}


