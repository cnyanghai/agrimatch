package com.agrimatch.post.dto;

public class PostQuery {
    private Long companyId;
    private Long userId;
    private String keyword;
    private String orderBy; // create_time
    private String order; // asc/desc
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

    public Long getViewerUserId() {
        return viewerUserId;
    }

    public void setViewerUserId(Long viewerUserId) {
        this.viewerUserId = viewerUserId;
    }
}


