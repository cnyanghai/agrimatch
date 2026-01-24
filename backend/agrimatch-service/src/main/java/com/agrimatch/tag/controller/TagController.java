package com.agrimatch.tag.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.tag.domain.NhtTag;
import com.agrimatch.tag.service.TagService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/list")
    public Result<List<NhtTag>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) Integer isHot) {
        return Result.success(tagService.getTagList(keyword, domain, isHot));
    }

    @GetMapping("/category/{categoryId}")
    public Result<List<NhtTag>> getTagsByCategoryId(@PathVariable Long categoryId) {
        return Result.success(tagService.getTagsByCategoryId(categoryId));
    }

    @GetMapping("/hot")
    public Result<List<NhtTag>> getHotTags() {
        return Result.success(tagService.getHotTags());
    }

    @GetMapping("/search")
    public Result<List<NhtTag>> searchTags(@RequestParam String keyword) {
        return Result.success(tagService.getTagList(keyword, null, null));
    }

    @GetMapping("/domains")
    public Result<List<String>> getTagDomains() {
        return Result.success(tagService.getTagDomains());
    }

    @PostMapping("/custom")
    public Result<NhtTag> createCustomTag(@RequestParam String tagName, @RequestParam(required = false, defaultValue = "general") String domain) {
        return Result.success(tagService.createCustomTag(tagName, domain));
    }

    @GetMapping("/{id}")
    public Result<NhtTag> getInfo(@PathVariable Integer id) {
        return Result.success(tagService.getTagById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Integer> remove(@PathVariable Integer id) {
        return Result.success(tagService.deleteTag(id));
    }
}

