package com.agrimatch.tag.mapper;

import com.agrimatch.tag.domain.NhtTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TagMapper {
    List<NhtTag> selectTagList(@Param("keyword") String keyword, @Param("domain") String domain, @Param("isHot") Integer isHot);
    
    List<NhtTag> selectTagsByCategoryId(@Param("categoryId") Long categoryId);
    
    NhtTag selectTagById(Integer id);
    
    int insertTag(NhtTag tag);
    
    int updateTag(NhtTag tag);
    
    int deleteTagById(Integer id);

    int deleteEntityTags(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    int insertEntityTagValue(@Param("entityType") String entityType, @Param("entityId") Long entityId, 
                             @Param("tagId") Integer tagId, @Param("tagKey") String tagKey,
                             @Param("tagValueText") String tagValueText, @Param("tagValueNum") Double tagValueNum,
                             @Param("domain") String domain);
}

