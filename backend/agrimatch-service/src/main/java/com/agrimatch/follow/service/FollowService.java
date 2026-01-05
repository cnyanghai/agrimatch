package com.agrimatch.follow.service;

import com.agrimatch.follow.dto.FollowedUserResponse;
import com.agrimatch.requirement.dto.RequirementResponse;
import com.agrimatch.supply.dto.SupplyResponse;

import java.util.List;

public interface FollowService {

    /**
     * 关注用户
     */
    void follow(Long userId, Long targetUserId);

    /**
     * 取消关注
     */
    void unfollow(Long userId, Long targetUserId);

    /**
     * 检查是否已关注
     */
    boolean isFollowing(Long userId, Long targetUserId);

    /**
     * 获取关注的用户列表
     */
    List<FollowedUserResponse> getFollowedUsers(Long userId);

    /**
     * 获取关注用户发布的采购需求
     */
    List<RequirementResponse> getFollowedRequirements(Long userId);

    /**
     * 获取关注用户发布的供应信息
     */
    List<SupplyResponse> getFollowedSupplies(Long userId);

    /**
     * 获取粉丝数
     */
    int getFollowerCount(Long userId);

    /**
     * 获取关注数
     */
    int getFollowingCount(Long userId);
}

