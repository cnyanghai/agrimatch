package com.agrimatch.search.mapper;

import com.agrimatch.search.dto.UnifiedSearchQuery;
import com.agrimatch.search.dto.UnifiedSearchResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SearchMapper {
    /**
     * 根据标签和板块进行搜索，并联合主表
     */
    List<UnifiedSearchResult> searchUnified(@Param("q") UnifiedSearchQuery query);
    
    long countUnified(@Param("q") UnifiedSearchQuery query);
}

