package com.agrimatch.follow.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.follow.dto.FollowedUserResponse;
import com.agrimatch.follow.service.FollowService;
import com.agrimatch.requirement.dto.RequirementResponse;
import com.agrimatch.supply.dto.SupplyResponse;
import com.agrimatch.post.dto.PostResponse;
import com.agrimatch.post.dto.PostQuery;
import com.agrimatch.post.service.PostService;
import com.agrimatch.util.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@Validated
public class FollowController {

    private final FollowService followService;
    private final PostService postService;

    public FollowController(FollowService followService, PostService postService) {
        this.followService = followService;
        this.postService = postService;
    }

    /**
     * 关注用户
     */
    @PostMapping("/{userId}")
    public Result<Void> follow(Authentication authentication, @PathVariable("userId") Long targetUserId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        followService.follow(userId, targetUserId);
        return Result.success();
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(Authentication authentication, @PathVariable("userId") Long targetUserId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        followService.unfollow(userId, targetUserId);
        return Result.success();
    }

    /**
     * 检查是否已关注某用户
     */
    @GetMapping("/check/{userId}")
    public Result<Boolean> checkFollowStatus(Authentication authentication, @PathVariable("userId") Long targetUserId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        boolean following = followService.isFollowing(userId, targetUserId);
        return Result.success(following);
    }

    /**
     * 获取我关注的用户列表
     */
    @GetMapping("/list")
    public Result<List<FollowedUserResponse>> getFollowedUsers(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        List<FollowedUserResponse> users = followService.getFollowedUsers(userId);
        return Result.success(users);
    }

    /**
     * 获取关注用户发布的采购需求
     */
    @GetMapping("/requirements")
    public Result<List<RequirementResponse>> getFollowedRequirements(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        List<RequirementResponse> requirements = followService.getFollowedRequirements(userId);
        return Result.success(requirements);
    }

    /**
     * 获取关注用户发布的供应信息
     */
    @GetMapping("/supplies")
    public Result<List<SupplyResponse>> getFollowedSupplies(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        List<SupplyResponse> supplies = followService.getFollowedSupplies(userId);
        return Result.success(supplies);
    }

    /**
     * 获取关注用户发布的观点帖子
     */
    @GetMapping("/posts")
    public Result<List<PostResponse>> getFollowedPosts(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        PostQuery q = new PostQuery();
        q.setViewerUserId(userId);
        q.setFollowingUserId(userId); // 需要在 PostQuery 中支持此字段
        return Result.success(postService.list(q));
    }

    /**
     * 获取某用户的粉丝数和关注数
     */
    @GetMapping("/stats/{userId}")
    public Result<FollowStats> getFollowStats(@PathVariable("userId") Long targetUserId) {
        int followers = followService.getFollowerCount(targetUserId);
        int following = followService.getFollowingCount(targetUserId);
        return Result.success(new FollowStats(followers, following));
    }

    public static class FollowStats {
        private int followers;
        private int following;

        public FollowStats(int followers, int following) {
            this.followers = followers;
            this.following = following;
        }

        public int getFollowers() {
            return followers;
        }

        public int getFollowing() {
            return following;
        }
    }
}

