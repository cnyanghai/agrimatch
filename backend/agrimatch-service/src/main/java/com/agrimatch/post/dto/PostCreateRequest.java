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
    
    private String postType;      // general/bounty/poll
    private Integer bountyPoints; // 悬赏积分

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

    public Integer getBountyPoints() {
        return bountyPoints;
    }

    public void setBountyPoints(Integer bountyPoints) {
        this.bountyPoints = bountyPoints;
    }
}


