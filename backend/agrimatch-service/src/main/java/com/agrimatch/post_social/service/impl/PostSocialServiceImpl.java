package com.agrimatch.post_social.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.post.mapper.PostMapper;
import com.agrimatch.post_social.domain.BusPostComment;
import com.agrimatch.post_social.domain.BusPostLike;
import com.agrimatch.post_social.domain.BusPostCollect;
import com.agrimatch.post_social.dto.PostCommentResponse;
import com.agrimatch.post_social.dto.PostLikeToggleResponse;
import com.agrimatch.post_social.mapper.PostCommentMapper;
import com.agrimatch.post_social.mapper.PostLikeMapper;
import com.agrimatch.post_social.mapper.PostCollectMapper;
import com.agrimatch.post_social.service.PostSocialService;
import com.agrimatch.notify.service.NotifyService;
import com.agrimatch.post.domain.BusPost;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostSocialServiceImpl implements PostSocialService {
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final PostLikeMapper likeMapper;
    private final PostCommentMapper commentMapper;
    private final PostCollectMapper collectMapper;
    private final NotifyService notifyService;

    public PostSocialServiceImpl(PostMapper postMapper, UserMapper userMapper, 
                               PostLikeMapper likeMapper, PostCommentMapper commentMapper, 
                               PostCollectMapper collectMapper, NotifyService notifyService) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.likeMapper = likeMapper;
        this.commentMapper = commentMapper;
        this.collectMapper = collectMapper;
        this.notifyService = notifyService;
    }

    @Override
    @Transactional
    public PostLikeToggleResponse toggleLike(Long userId, Long postId) {
        if (userId == null) throw new ApiException(401, "未登录");
        if (postId == null) throw new ApiException(ResultCode.PARAM_ERROR);
        BusPost post = postMapper.selectById(postId);
        if (post == null) throw new ApiException(ResultCode.NOT_FOUND);

        BusPostLike existed = likeMapper.selectByPostAndUser(postId, userId);
        boolean liked;
        if (existed == null) {
            BusPostLike l = new BusPostLike();
            l.setPostId(postId);
            l.setUserId(userId);
            int rows = likeMapper.insert(l);
            if (rows != 1) throw new ApiException(ResultCode.SERVER_ERROR);
            liked = true;
        } else {
            int newDel = (existed.getIsDeleted() != null && existed.getIsDeleted() == 0) ? 1 : 0;
            int rows = likeMapper.updateIsDeleted(existed.getId(), newDel);
            if (rows != 1) throw new ApiException(ResultCode.SERVER_ERROR);
            liked = (newDel == 0);
        }

        PostLikeToggleResponse r = new PostLikeToggleResponse();
        r.setLiked(liked);
        r.setLikeCount(likeMapper.countByPostId(postId));

        // 通知：有人点赞你的帖子
        if (liked && post.getUserId() != null && !post.getUserId().equals(userId)) {
            notifyService.send(
                    post.getUserId(),
                    "POST_LIKE",
                    "你的帖子收到点赞",
                    "有人点赞了你的帖子：《" + post.getTitle() + "》",
                    "/posts"
            );
        }
        return r;
    }

    @Override
    public List<PostCommentResponse> listComments(Long postId) {
        if (postId == null) throw new ApiException(ResultCode.PARAM_ERROR);
        List<BusPostComment> list = commentMapper.selectByPostId(postId);
        List<PostCommentResponse> out = new ArrayList<>();
        for (BusPostComment c : list) {
            PostCommentResponse r = new PostCommentResponse();
            r.setId(c.getId());
            r.setPostId(c.getPostId());
            r.setCompanyId(c.getCompanyId());
            r.setUserId(c.getUserId());
            r.setCompanyName(c.getCompanyName());
            r.setUserName(c.getUserName());
            r.setNickName(c.getNickName());
            r.setContent(c.getContent());
            r.setCreateTime(c.getCreateTime());
            out.add(r);
        }
        return out;
    }

    @Override
    @Transactional
    public Long addComment(Long userId, Long postId, String content) {
        if (userId == null) throw new ApiException(401, "未登录");
        if (postId == null || !StringUtils.hasText(content)) throw new ApiException(ResultCode.PARAM_ERROR);
        BusPost post = postMapper.selectById(postId);
        if (post == null) throw new ApiException(ResultCode.NOT_FOUND);

        SysUser u = userMapper.selectById(userId);
        if (u == null) throw new ApiException(401, "未登录");

        BusPostComment c = new BusPostComment();
        c.setPostId(postId);
        c.setCompanyId(u.getCompanyId());
        c.setUserId(userId);
        c.setContent(content.trim());
        int rows = commentMapper.insert(c);
        if (rows != 1 || c.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);

        // 通知：有人评论你的帖子
        if (post.getUserId() != null && !post.getUserId().equals(userId)) {
            notifyService.send(
                    post.getUserId(),
                    "POST_COMMENT",
                    "你的帖子收到评论",
                    "有人评论了你的帖子：《" + post.getTitle() + "》",
                    "/posts"
            );
        }
        return c.getId();
    }

    @Override
    @Transactional
    public boolean toggleCollect(Long userId, Long postId) {
        if (userId == null) throw new ApiException(401, "未登录");
        if (postId == null) throw new ApiException(ResultCode.PARAM_ERROR);
        BusPost post = postMapper.selectById(postId);
        if (post == null) throw new ApiException(ResultCode.NOT_FOUND);

        BusPostCollect existed = collectMapper.selectOne(userId, postId);
        if (existed == null) {
            BusPostCollect c = new BusPostCollect();
            c.setUserId(userId);
            c.setPostId(postId);
            collectMapper.insert(c);
            return true;
        } else {
            collectMapper.delete(userId, postId);
            return false;
        }
    }

    @Override
    public boolean isCollected(Long userId, Long postId) {
        if (userId == null || postId == null) return false;
        return collectMapper.selectOne(userId, postId) != null;
    }

    @Override
    public List<Long> listCollectedPostIds(Long userId) {
        if (userId == null) return new ArrayList<>();
        return collectMapper.selectCollectedPostIds(userId, null);
    }
}


