package com.agrimatch.search.service.impl;

import com.agrimatch.common.api.PageResult;
import com.agrimatch.search.dto.UnifiedSearchQuery;
import com.agrimatch.search.dto.UnifiedSearchResult;
import com.agrimatch.search.mapper.SearchMapper;
import com.agrimatch.search.service.UnifiedSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnifiedSearchServiceImpl implements UnifiedSearchService {

    private final SearchMapper searchMapper;

    @Override
    public PageResult<UnifiedSearchResult> search(UnifiedSearchQuery query) {
        List<UnifiedSearchResult> list = searchMapper.searchUnified(query);
        long total = searchMapper.countUnified(query);
        return new PageResult<>(list, total, query.getPage(), query.getSize());
    }
}

