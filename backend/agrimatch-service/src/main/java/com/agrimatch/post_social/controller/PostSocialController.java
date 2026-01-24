package com.agrimatch.post_social.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.post_social.dto.PostCommentCreateRequest;
import com.agrimatch.post_social.dto.PostCommentResponse;
import com.agrimatch.post_social.dto.PostLikeToggleResponse;
import com.agrimatch.post_social.service.PostSocialService;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Validated
public class PostSocialController {
    private final PostSocialService postSocialService;

    public PostSocialController(PostSocialService postSocialService) {
        this.postSocialService = postSocialService;
    }

    @PostMapping("/{postId}/like")
    public Result<PostLikeToggleResponse> toggleLike(Authentication authentication, @PathVariable("postId") @NotNull Long postId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(postSocialService.toggleLike(userId, postId));
    }

    @GetMapping("/{postId}/comments")
    public Result<List<PostCommentResponse>> comments(@PathVariable("postId") @NotNull Long postId) {
        return Result.success(postSocialService.listComments(postId));
    }

    @PostMapping("/{postId}/comments")
    public Result<Long> addComment(Authentication authentication,
                                  @PathVariable("postId") @NotNull Long postId,
                                  @Valid @RequestBody PostCommentCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(postSocialService.addComment(userId, postId, req.getContent()));
    }

    @PostMapping("/{postId}/collect")
    public Result<Boolean> toggleCollect(Authentication authentication, @PathVariable("postId") @NotNull Long postId) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(postSocialService.toggleCollect(userId, postId));
    }

    @GetMapping("/{postId}/collect/status")
    public Result<Boolean> collectStatus(Authentication authentication, @PathVariable("postId") @NotNull Long postId) {
        Long userId = SecurityUtil.getUserIdOrNull(authentication);
        return Result.success(postSocialService.isCollected(userId, postId));
    }

    @GetMapping("/collected")
    public Result<List<Long>> collected(Authentication authentication) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(postSocialService.listCollectedPostIds(userId));
    }
}


