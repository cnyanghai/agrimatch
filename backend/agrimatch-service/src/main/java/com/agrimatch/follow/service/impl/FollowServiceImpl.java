package com.agrimatch.follow.service.impl;

import com.agrimatch.follow.domain.BusUserFollow;
import com.agrimatch.follow.dto.FollowedUserResponse;
import com.agrimatch.follow.mapper.FollowMapper;
import com.agrimatch.follow.service.FollowService;
import com.agrimatch.requirement.dto.RequirementResponse;
import com.agrimatch.supply.dto.SupplyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;

    public FollowServiceImpl(FollowMapper followMapper) {
        this.followMapper = followMapper;
    }

    @Override
    @Transactional
    public void follow(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("不能关注自己");
        }

        // 检查是否已关注
        BusUserFollow existing = followMapper.selectByUserAndFollowUser(userId, targetUserId);
        if (existing != null) {
            return; // 已关注，忽略
        }

        BusUserFollow follow = new BusUserFollow();
        follow.setUserId(userId);
        follow.setFollowUserId(targetUserId);
        followMapper.insert(follow);
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long targetUserId) {
        followMapper.deleteByUserAndFollowUser(userId, targetUserId);
    }

    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        BusUserFollow follow = followMapper.selectByUserAndFollowUser(userId, targetUserId);
        return follow != null;
    }

    @Override
    public List<FollowedUserResponse> getFollowedUsers(Long userId) {
        return followMapper.selectFollowedUsers(userId);
    }

    @Override
    public List<RequirementResponse> getFollowedRequirements(Long userId) {
        return followMapper.selectFollowedRequirements(userId);
    }

    @Override
    public List<SupplyResponse> getFollowedSupplies(Long userId) {
        return followMapper.selectFollowedSupplies(userId);
    }

    @Override
    public int getFollowerCount(Long userId) {
        return followMapper.countFollowers(userId);
    }

    @Override
    public int getFollowingCount(Long userId) {
        return followMapper.countFollowing(userId);
    }
}

