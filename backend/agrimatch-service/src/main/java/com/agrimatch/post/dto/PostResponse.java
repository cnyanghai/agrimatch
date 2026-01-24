package com.agrimatch.post.dto;

import java.time.LocalDateTime;

public class PostResponse {
    private Long id;
    private Long companyId;
    private Long userId;
    private String companyName;
    private String userName;
    private String nickName;
    private String position;
    private String title;
    private String content;
    private String domain;
    private String tagsJson;
    private String imagesJson;
    private String postType;
    private Integer bountyPoints;
    private Integer bountyStatus;
    private Long acceptedCommentId;
    private LocalDateTime createTime;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean likedByMe;

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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTagsJson() {
        return tagsJson;
    }

    public void setTagsJson(String tagsJson) {
        this.tagsJson = tagsJson;
    }

    public String getImagesJson() {
        return imagesJson;
    }

    public void setImagesJson(String imagesJson) {
        this.imagesJson = imagesJson;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Boolean getLikedByMe() {
        return likedByMe;
    }

    public void setLikedByMe(Boolean likedByMe) {
        this.likedByMe = likedByMe;
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


