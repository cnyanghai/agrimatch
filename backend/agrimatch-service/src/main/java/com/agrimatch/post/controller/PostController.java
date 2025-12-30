package com.agrimatch.post.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.post.dto.PostCreateRequest;
import com.agrimatch.post.dto.PostQuery;
import com.agrimatch.post.dto.PostResponse;
import com.agrimatch.post.service.PostService;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public Result<Long> create(Authentication authentication, @Valid @RequestBody PostCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(postService.create(userId, req));
    }

    @GetMapping("/{id}")
    public Result<PostResponse> getById(@PathVariable("id") @NotNull Long id) {
        return Result.success(postService.getById(id));
    }

    @GetMapping
    public Result<List<PostResponse>> list(Authentication authentication, PostQuery q) {
        Long userId = SecurityUtil.requireUserId(authentication);
        if (q == null) q = new PostQuery();
        q.setViewerUserId(userId);
        return Result.success(postService.list(q));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @NotNull Long id) {
        postService.delete(id);
        return Result.success();
    }
}


