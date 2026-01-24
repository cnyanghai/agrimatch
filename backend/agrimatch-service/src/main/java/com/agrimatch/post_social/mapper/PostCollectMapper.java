package com.agrimatch.post_social.mapper;

import com.agrimatch.post_social.domain.BusPostCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostCollectMapper {
    int insert(BusPostCollect collect);

    int delete(@Param("userId") Long userId, @Param("postId") Long postId);

    BusPostCollect selectOne(@Param("userId") Long userId, @Param("postId") Long postId);

    List<Long> selectCollectedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}

