package com.agrimatch.post.mapper;

import com.agrimatch.post.domain.BusPost;
import com.agrimatch.post.dto.PostQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostMapper {
    int insert(BusPost p);

    BusPost selectById(@Param("id") Long id);

    List<BusPost> selectList(@Param("q") PostQuery q);

    int logicalDelete(@Param("id") Long id);
}


