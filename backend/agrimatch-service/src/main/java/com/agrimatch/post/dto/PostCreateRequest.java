package com.agrimatch.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostCreateRequest {
    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 20000)
    private String content;

    private String imagesJson;
    
    private String domain;        // 所属板块
    private String tagsJson;      // 标签数据
    
    private String postType;      // general/poll/paid
    private Boolean isPaid;
    private java.math.BigDecimal price;
    private Integer teaserLength;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImagesJson() {
        return imagesJson;
    }

    public void setImagesJson(String imagesJson) {
        this.imagesJson = imagesJson;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTagsJson() {
        return tagsJson;
    }

    public void setTagsJson(String tagsJson) {
        this.tagsJson = tagsJson;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public java.math.BigDecimal getPrice() {
        return price;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }

    public Integer getTeaserLength() {
        return teaserLength;
    }

    public void setTeaserLength(Integer teaserLength) {
        this.teaserLength = teaserLength;
    }
}


