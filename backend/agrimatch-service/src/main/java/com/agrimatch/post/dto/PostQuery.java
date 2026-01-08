package com.agrimatch.post.dto;

public class PostQuery {
    private Long companyId;
    private Long userId;
    private String keyword;
    private String postType; // general/bounty/poll
    private String orderBy; // create_time
    private String order; // asc/desc
    private Integer recentDays; // 用于 hot_7d：仅查询最近 N 天内发布的帖子
    private Integer limit; // 返回条数限制（首页等场景）
    private Long viewerUserId; // 用于计算“我是否点赞”，不参与过滤

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public Integer getRecentDays() {
        return recentDays;
    }

    public void setRecentDays(Integer recentDays) {
        this.recentDays = recentDays;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Long getViewerUserId() {
        return viewerUserId;
    }

    public void setViewerUserId(Long viewerUserId) {
        this.viewerUserId = viewerUserId;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }
}


