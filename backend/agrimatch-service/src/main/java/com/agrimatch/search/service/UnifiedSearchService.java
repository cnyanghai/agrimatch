package com.agrimatch.search.service;

import com.agrimatch.common.api.PageResult;
import com.agrimatch.search.dto.UnifiedSearchQuery;
import com.agrimatch.search.dto.UnifiedSearchResult;

public interface UnifiedSearchService {
    PageResult<UnifiedSearchResult> search(UnifiedSearchQuery query);
}

