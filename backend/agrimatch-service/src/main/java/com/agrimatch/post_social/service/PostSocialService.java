package com.agrimatch.post_social.service;

import com.agrimatch.post_social.dto.PostCommentResponse;
import com.agrimatch.post_social.dto.PostLikeToggleResponse;

import java.util.List;

public interface PostSocialService {
    PostLikeToggleResponse toggleLike(Long userId, Long postId);

    List<PostCommentResponse> listComments(Long postId);

    Long addComment(Long userId, Long postId, String content);
}


