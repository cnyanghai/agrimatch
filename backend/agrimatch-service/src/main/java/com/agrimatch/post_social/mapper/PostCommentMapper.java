package com.agrimatch.post_social.mapper;

import com.agrimatch.post_social.domain.BusPostComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostCommentMapper {
    int insert(BusPostComment c);

    BusPostComment selectById(@Param("id") Long id);

    List<BusPostComment> selectByPostId(@Param("postId") Long postId);

    int countByPostId(@Param("postId") Long postId);

    int updateAccepted(@Param("id") Long id);

    List<PostIdCountRow> countByPostIds(@Param("postIds") List<Long> postIds);

    class PostIdCountRow {
        private Long postId;
        private Integer cnt;

        public Long getPostId() {
            return postId;
        }

        public void setPostId(Long postId) {
            this.postId = postId;
        }

        public Integer getCnt() {
            return cnt;
        }

        public void setCnt(Integer cnt) {
            this.cnt = cnt;
        }
    }
}


