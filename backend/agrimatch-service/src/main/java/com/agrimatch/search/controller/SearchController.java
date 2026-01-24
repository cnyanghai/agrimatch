package com.agrimatch.search.controller;

import com.agrimatch.common.api.PageResult;
import com.agrimatch.common.api.Result;
import com.agrimatch.search.dto.UnifiedSearchQuery;
import com.agrimatch.search.dto.UnifiedSearchResult;
import com.agrimatch.search.service.UnifiedSearchService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final UnifiedSearchService unifiedSearchService;
    private final ObjectMapper objectMapper;

    @GetMapping("/unified")
    public Result<PageResult<UnifiedSearchResult>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String tagFiltersJson,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        UnifiedSearchQuery query = new UnifiedSearchQuery();
        query.setKeyword(keyword);
        query.setDomain(domain);
        query.setEntityType(entityType);
        query.setPage(page);
        query.setSize(size);
        
        if (tagFiltersJson != null && !tagFiltersJson.isEmpty()) {
            try {
                Map<String, Object> filters = objectMapper.readValue(tagFiltersJson, new TypeReference<Map<String, Object>>() {});
                query.setTagFilters(filters);
            } catch (Exception e) {
                log.error("Failed to parse tagFiltersJson: {}", tagFiltersJson, e);
            }
        }
        
        return Result.success(unifiedSearchService.search(query));
    }
}

