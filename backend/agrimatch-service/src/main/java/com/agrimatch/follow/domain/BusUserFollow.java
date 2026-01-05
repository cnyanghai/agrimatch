package com.agrimatch.follow.domain;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体
 */
public class BusUserFollow {
    private Long id;
    private Long userId;           // 关注者ID
    private Long followUserId;     // 被关注者ID
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFollowUserId() {
        return followUserId;
    }

    public void setFollowUserId(Long followUserId) {
        this.followUserId = followUserId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

