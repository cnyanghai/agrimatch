package com.agrimatch.chat.mapper;

import com.agrimatch.chat.domain.BusChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMapper {
    int insertMessage(BusChatMessage m);

    List<BusChatMessage> selectHistory(@Param("a") Long a, @Param("b") Long b, @Param("limit") Integer limit);

    int markReadFromPeer(@Param("toUserId") Long toUserId, @Param("fromUserId") Long fromUserId);

    List<PeerRow> selectPeers(@Param("userId") Long userId);

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
}


