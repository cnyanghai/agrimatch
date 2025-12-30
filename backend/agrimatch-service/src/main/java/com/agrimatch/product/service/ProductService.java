package com.agrimatch.product.service;

import com.agrimatch.product.dto.ProductCreateRequest;
import com.agrimatch.product.dto.ProductNode;

import java.util.List;

public interface ProductService {
    List<ProductNode> tree();

    List<ProductNode> search(String keyword);

    Long createCustom(Long userId, ProductCreateRequest req);
}


