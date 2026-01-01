package com.agrimatch.post.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.post.domain.BusPost;
import com.agrimatch.post.dto.PostCreateRequest;
import com.agrimatch.post.dto.PostQuery;
import com.agrimatch.post.dto.PostResponse;
import com.agrimatch.post.mapper.PostMapper;
import com.agrimatch.post.service.PostService;
import com.agrimatch.post_social.mapper.PostCommentMapper;
import com.agrimatch.post_social.mapper.PostLikeMapper;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl implements PostService {
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCommentMapper postCommentMapper;

    public PostServiceImpl(PostMapper postMapper, UserMapper userMapper, PostLikeMapper postLikeMapper, PostCommentMapper postCommentMapper) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.postLikeMapper = postLikeMapper;
        this.postCommentMapper = postCommentMapper;
    }

    @Override
    public Long create(Long userId, PostCreateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        SysUser u = userMapper.selectById(userId);
        if (u == null) throw new ApiException(401, "未登录");

        BusPost p = new BusPost();
        p.setUserId(userId);
        p.setCompanyId(u.getCompanyId()); // 允许为空
        p.setTitle(req.getTitle());
        p.setContent(emptyToNull(req.getContent()));
        p.setImagesJson(emptyToNull(req.getImagesJson()));

        int rows = postMapper.insert(p);
        if (rows != 1 || p.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);
        return p.getId();
    }

    @Override
    public PostResponse getById(Long id) {
        BusPost p = postMapper.selectById(id);
        if (p == null) throw new ApiException(ResultCode.NOT_FOUND);
        PostResponse r = toResponse(p);
        r.setLikeCount(postLikeMapper.countByPostId(id));
        r.setCommentCount(postCommentMapper.countByPostId(id));
        r.setLikedByMe(false);
        return r;
    }

    @Override
    public List<PostResponse> list(PostQuery q) {
        normalizeQuery(q);
        List<BusPost> list = postMapper.selectList(q);
        List<PostResponse> out = new ArrayList<>();
        if (list.isEmpty()) return out;

        List<Long> postIds = new ArrayList<>();
        for (BusPost p : list) postIds.add(p.getId());

        Map<Long, Integer> likeCntMap = new HashMap<>();
        for (PostLikeMapper.PostIdCountRow row : postLikeMapper.countByPostIds(postIds)) {
            likeCntMap.put(row.getPostId(), row.getCnt());
        }
        Map<Long, Integer> commentCntMap = new HashMap<>();
        for (PostCommentMapper.PostIdCountRow row : postCommentMapper.countByPostIds(postIds)) {
            commentCntMap.put(row.getPostId(), row.getCnt());
        }

        Map<Long, Boolean> likedByMeMap = new HashMap<>();
        if (q != null && q.getViewerUserId() != null) {
            for (Long pid : postLikeMapper.selectLikedPostIds(q.getViewerUserId(), postIds)) {
                likedByMeMap.put(pid, true);
            }
        }

        for (BusPost p : list) {
            PostResponse r = toResponse(p);
            r.setLikeCount(likeCntMap.getOrDefault(p.getId(), 0));
            r.setCommentCount(commentCntMap.getOrDefault(p.getId(), 0));
            r.setLikedByMe(likedByMeMap.getOrDefault(p.getId(), false));
            out.add(r);
        }
        return out;
    }

    @Override
    public void delete(Long id) {
        int rows = postMapper.logicalDelete(id);
        if (rows != 1) throw new ApiException(ResultCode.NOT_FOUND);
    }

    private static PostResponse toResponse(BusPost p) {
        PostResponse r = new PostResponse();
        r.setId(p.getId());
        r.setCompanyId(p.getCompanyId());
        r.setUserId(p.getUserId());
        r.setCompanyName(p.getCompanyName());
        r.setUserName(p.getUserName());
        r.setNickName(p.getNickName());
        r.setPosition(p.getPosition());
        r.setTitle(p.getTitle());
        r.setContent(p.getContent());
        r.setImagesJson(p.getImagesJson());
        r.setCreateTime(p.getCreateTime());
        return r;
    }

    private static void normalizeQuery(PostQuery q) {
        if (q == null) return;
        if (!StringUtils.hasText(q.getOrderBy())) q.setOrderBy("create_time");
        if (!StringUtils.hasText(q.getOrder())) q.setOrder("desc");
        else q.setOrder(q.getOrder().toLowerCase());

        // 首页热门话题：近 N 天热度
        if ("hot_7d".equals(q.getOrderBy())) {
            if (q.getRecentDays() == null || q.getRecentDays() <= 0) q.setRecentDays(7);
            // hot 默认 desc
            q.setOrder("desc");
            // 默认只取少量（防止误传空 limit 拉全表）
            if (q.getLimit() == null || q.getLimit() <= 0) q.setLimit(2);
        }
    }

    private static String emptyToNull(String s) {
        return StringUtils.hasText(s) ? s : null;
    }
}


