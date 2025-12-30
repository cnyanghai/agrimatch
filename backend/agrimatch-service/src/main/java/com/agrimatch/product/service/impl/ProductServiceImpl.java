package com.agrimatch.product.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.product.domain.NhtProduct;
import com.agrimatch.product.dto.ProductCreateRequest;
import com.agrimatch.product.dto.ProductNode;
import com.agrimatch.product.mapper.ProductMapper;
import com.agrimatch.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductNode> tree() {
        List<NhtProduct> list = productMapper.selectAllActive();
        return buildTree(list);
    }

    @Override
    public List<ProductNode> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        List<NhtProduct> list = productMapper.search(keyword.trim());
        List<ProductNode> out = new ArrayList<>();
        for (NhtProduct p : list) {
            out.add(new ProductNode(p.getId(), p.getParentId(), p.getProductName()));
        }
        return out;
    }

    @Override
    public Long createCustom(Long userId, ProductCreateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        if (req == null || !StringUtils.hasText(req.getName())) throw new ApiException(ResultCode.PARAM_ERROR);
        NhtProduct p = new NhtProduct();
        p.setUserId(userId);
        p.setParentId(req.getParentId() == null ? 0L : req.getParentId());
        p.setProductName(req.getName().trim());
        int rows = productMapper.insertCustom(p);
        if (rows != 1 || p.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);
        return p.getId();
    }

    private static List<ProductNode> buildTree(List<NhtProduct> list) {
        Map<Long, ProductNode> map = new HashMap<>();
        for (NhtProduct p : list) {
            map.put(p.getId(), new ProductNode(p.getId(), p.getParentId(), p.getProductName()));
        }
        List<ProductNode> roots = new ArrayList<>();
        for (ProductNode n : map.values()) {
            Long pid = n.getParentId();
            if (pid == null || pid == 0L) {
                roots.add(n);
            } else {
                ProductNode parent = map.get(pid);
                if (parent != null) {
                    parent.getChildren().add(n);
                } else {
                    roots.add(n);
                }
            }
        }
        return roots;
    }
}


