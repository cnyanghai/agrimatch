package com.agrimatch.post_social.mapper;

import com.agrimatch.post_social.domain.BusPostLike;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostLikeMapper {
    BusPostLike selectByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);

    int insert(BusPostLike like);

    int updateIsDeleted(@Param("id") Long id, @Param("isDeleted") Integer isDeleted);

    int countByPostId(@Param("postId") Long postId);

    List<Long> selectLikedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

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


