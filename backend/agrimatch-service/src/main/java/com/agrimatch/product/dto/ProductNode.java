package com.agrimatch.product.dto;

import java.util.ArrayList;
import java.util.List;

public class ProductNode {
    private Long id;
    private Long parentId;
    private String name;
    private List<ProductNode> children = new ArrayList<>();

    public ProductNode() {
    }

    public ProductNode(Long id, Long parentId, String name) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProductNode> getChildren() {
        return children;
    }

    public void setChildren(List<ProductNode> children) {
        this.children = children;
    }
}


