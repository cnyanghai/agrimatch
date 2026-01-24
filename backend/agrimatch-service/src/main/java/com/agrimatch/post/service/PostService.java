package com.agrimatch.post.service;

import com.agrimatch.post.dto.PostCreateRequest;
import com.agrimatch.post.dto.PostQuery;
import com.agrimatch.post.dto.PostResponse;

import java.util.List;

public interface PostService {
    Long create(Long userId, PostCreateRequest req);

    PostResponse getById(Long id);

    List<PostResponse> list(PostQuery q);

    void delete(Long id);
}


