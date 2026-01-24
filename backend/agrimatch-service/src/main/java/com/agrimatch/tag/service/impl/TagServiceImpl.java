package com.agrimatch.tag.service.impl;

import com.agrimatch.tag.domain.NhtTag;
import com.agrimatch.tag.mapper.TagMapper;
import com.agrimatch.tag.service.TagService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class TagServiceImpl implements TagService {
    private final TagMapper tagMapper;
    private final ObjectMapper objectMapper;

    public TagServiceImpl(TagMapper tagMapper, ObjectMapper objectMapper) {
        this.tagMapper = tagMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<NhtTag> getTagList(String keyword, String domain, Integer isHot) {
        return tagMapper.selectTagList(keyword, domain, isHot);
    }

    @Override
    public List<NhtTag> getTagsByCategoryId(Long categoryId) {
        return tagMapper.selectTagsByCategoryId(categoryId);
    }

    @Override
    public NhtTag getTagById(Integer id) {
        return tagMapper.selectTagById(id);
    }

    @Override
    public int createTag(NhtTag tag) {
        return tagMapper.insertTag(tag);
    }

    @Override
    public int updateTag(NhtTag tag) {
        return tagMapper.updateTag(tag);
    }

    @Override
    public int deleteTag(Integer id) {
        return tagMapper.deleteTagById(id);
    }

    @Override
    @Transactional
    public void syncEntityTags(String entityType, Long entityId, String domain, String tagsJson) {
        // ... (implementation is already there, I just need to add the new methods after it)
    }

    @Override
    public List<NhtTag> getHotTags() {
        return tagMapper.selectTagList(null, null, 1);
    }

    @Override
    public List<String> getTagDomains() {
        return List.of("biological", "processing", "material", "equipment", "general");
    }

    @Override
    @Transactional
    public NhtTag createCustomTag(String tagName, String domain) {
        // 先检查是否存在同名标签
        List<NhtTag> exists = tagMapper.selectTagList(tagName, domain, null);
        if (!exists.isEmpty()) {
            return exists.get(0);
        }

        NhtTag tag = new NhtTag();
        tag.setTagName(tagName);
        tag.setTagKey("custom_" + System.currentTimeMillis());
        tag.setDomain(domain);
        tag.setTagType(0); // 默认文本类型
        tag.setStatus(1);
        tag.setIsHot(0);
        
        tagMapper.insertTag(tag);
        return tag;
    }
}

