package com.agrimatch.tag.service;

import com.agrimatch.tag.domain.NhtTag;
import java.util.List;

public interface TagService {
    List<NhtTag> getTagList(String keyword, String domain, Integer isHot);
    List<NhtTag> getTagsByCategoryId(Long categoryId);
    NhtTag getTagById(Integer id);
    int createTag(NhtTag tag);
    int updateTag(NhtTag tag);
    int deleteTag(Integer id);

    /**
     * 同步实体的标签数据到扁平化索引表
     * @param entityType 实体类型: supply, requirement, post, company
     * @param entityId 实体ID
     * @param domain 所属领域
     * @param tagsJson 标签JSON数据
     */
    void syncEntityTags(String entityType, Long entityId, String domain, String tagsJson);

    List<NhtTag> getHotTags();

    List<String> getTagDomains();

    NhtTag createCustomTag(String tagName, String domain);
}

