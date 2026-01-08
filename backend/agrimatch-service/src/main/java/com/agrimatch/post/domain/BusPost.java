package com.agrimatch.post.domain;

import java.time.LocalDateTime;

public class BusPost {
    private Long id;
    private Long companyId;
    private Long userId;

    // display fields (joined)
    private String companyName;
    private String userName;
    private String nickName;
    private String position;
    private String title;
    private String content;
    private String imagesJson;
    private String postType;          // 帖子类型: general/bounty/poll
    private Integer bountyPoints;     // 悬赏积分
    private Integer bountyStatus;     // 悬赏状态: 0=进行中, 1=已采纳, 2=已过期
    private Long acceptedCommentId;   // 被采纳的评论ID
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImagesJson() {
        return imagesJson;
    }

    public void setImagesJson(String imagesJson) {
        this.imagesJson = imagesJson;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public Integer getBountyPoints() {
        return bountyPoints;
    }

    public void setBountyPoints(Integer bountyPoints) {
        this.bountyPoints = bountyPoints;
    }

    public Integer getBountyStatus() {
        return bountyStatus;
    }

    public void setBountyStatus(Integer bountyStatus) {
        this.bountyStatus = bountyStatus;
    }

    public Long getAcceptedCommentId() {
        return acceptedCommentId;
    }

    public void setAcceptedCommentId(Long acceptedCommentId) {
        this.acceptedCommentId = acceptedCommentId;
    }
}


