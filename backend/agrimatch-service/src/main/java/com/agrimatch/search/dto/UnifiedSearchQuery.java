package com.agrimatch.search.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class UnifiedSearchQuery {
    private String keyword;
    private String domain;      // biological, processing, material, equipment, general
    private String entityType;  // supply, requirement, post
    private Map<String, Object> tagFilters; // tagKey -> value (String or Number)
    private int page = 1;
    private int size = 10;
}

