package com.agrimatch.search.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UnifiedSearchResult {
    private String entityType; // supply, requirement, post
    private Long entityId;
    private String title;      // categoryName for supply/req, title for post
    private String content;    // remark for supply/req, content for post
    private String domain;
    private String tagsJson;
    private String userName;
    private String companyName;
    private String imageUrl;   // first image if any
    private LocalDateTime createTime;
    
    // Additional fields for specific types
    private Object extra;      // Full DTO or specific fields
}

