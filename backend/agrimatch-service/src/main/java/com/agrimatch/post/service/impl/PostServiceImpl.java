package com.agrimatch.post.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.points.service.PointsService;
import com.agrimatch.post.domain.BusPost;
import com.agrimatch.post.dto.PostCreateRequest;
import com.agrimatch.post.dto.PostQuery;
import com.agrimatch.post.dto.PostResponse;
import com.agrimatch.post.mapper.PostMapper;
import com.agrimatch.post.service.PostService;
import com.agrimatch.tag.service.TagService;
import com.agrimatch.post_social.domain.BusPostComment;
import com.agrimatch.post_social.mapper.PostCommentMapper;
import com.agrimatch.post_social.mapper.PostLikeMapper;
import com.agrimatch.post_social.mapper.PostCollectMapper;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final PostCollectMapper postCollectMapper;
    private final PointsService pointsService;
    private final TagService tagService;

    public PostServiceImpl(PostMapper postMapper, UserMapper userMapper, 
                           PostLikeMapper postLikeMapper, PostCommentMapper postCommentMapper,
                           PostCollectMapper postCollectMapper,
                           PointsService pointsService, TagService tagService) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.postLikeMapper = postLikeMapper;
        this.postCommentMapper = postCommentMapper;
        this.postCollectMapper = postCollectMapper;
        this.pointsService = pointsService;
        this.tagService = tagService;
    }

    @Override
    @Transactional
    public Long create(Long userId, PostCreateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        SysUser u = userMapper.selectById(userId);
        if (u == null) throw new ApiException(401, "未登录");

        String postType = StringUtils.hasText(req.getPostType()) ? req.getPostType() : "general";

        BusPost p = new BusPost();
        p.setUserId(userId);
        p.setCompanyId(u.getCompanyId()); // 允许为空
        p.setTitle(req.getTitle());
        p.setContent(emptyToNull(req.getContent()));
        p.setDomain(req.getDomain() != null ? req.getDomain() : "general");
        p.setTagsJson(req.getTagsJson());
        p.setImagesJson(emptyToNull(req.getImagesJson()));
        p.setPostType(postType);
        p.setIsPaid(false);
        p.setIsExpert(false);
        
        // 付费设置
        if ("paid".equals(postType)) {
            p.setIsPaid(true);
            p.setPrice(req.getPrice());
            p.setTeaserLength(req.getTeaserLength() != null ? req.getTeaserLength() : 100);
        }
        
        // 专家标记 (简化：如果用户有 position 且包含专家字样，则自动标记)
        if (u.getPosition() != null && u.getPosition().contains("专家")) {
            p.setIsExpert(true);
        }

        int rows = postMapper.insert(p);
        if (rows != 1 || p.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);

        // 同步标签
        tagService.syncEntityTags("post", p.getId(), p.getDomain(), p.getTagsJson());

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
        r.setCollectedByMe(false);
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
        Map<Long, Boolean> collectedByMeMap = new HashMap<>();
        if (q != null && q.getViewerUserId() != null) {
            for (Long pid : postLikeMapper.selectLikedPostIds(q.getViewerUserId(), postIds)) {
                likedByMeMap.put(pid, true);
            }
            for (Long pid : postCollectMapper.selectCollectedPostIds(q.getViewerUserId(), postIds)) {
                collectedByMeMap.put(pid, true);
            }
        }

        for (BusPost p : list) {
            PostResponse r = toResponse(p);
            r.setLikeCount(likeCntMap.getOrDefault(p.getId(), 0));
            r.setCommentCount(commentCntMap.getOrDefault(p.getId(), 0));
            r.setLikedByMe(likedByMeMap.getOrDefault(p.getId(), false));
            r.setCollectedByMe(collectedByMeMap.getOrDefault(p.getId(), false));
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
        r.setPostType(p.getPostType());
        r.setIsPaid(Boolean.TRUE.equals(p.getIsPaid()));
        r.setPrice(p.getPrice());
        r.setTeaserLength(p.getTeaserLength());
        r.setIsExpert(Boolean.TRUE.equals(p.getIsExpert()));
        r.setDomain(p.getDomain());
        r.setTagsJson(p.getTagsJson());
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


