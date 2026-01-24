package com.agrimatch.tag.domain;

import java.time.LocalDateTime;

public class NhtTag {
    private Integer id;
    private String tagName;
    private String tagKey;
    private String domain;
    private Integer tagType;
    private String unit;
    private String options;
    private String recommendCategories;
    private Integer isHot;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public String getTagKey() { return tagKey; }
    public void setTagKey(String tagKey) { this.tagKey = tagKey; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public Integer getTagType() { return tagType; }
    public void setTagType(Integer tagType) { this.tagType = tagType; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getRecommendCategories() { return recommendCategories; }
    public void setRecommendCategories(String recommendCategories) { this.recommendCategories = recommendCategories; }

    public Integer getIsHot() { return isHot; }
    public void setIsHot(Integer isHot) { this.isHot = isHot; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

