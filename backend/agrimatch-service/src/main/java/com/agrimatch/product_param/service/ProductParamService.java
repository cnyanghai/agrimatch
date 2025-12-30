package com.agrimatch.product_param.service;

import com.agrimatch.product_param.dto.ProductParamResponse;

import java.util.List;

public interface ProductParamService {
    List<ProductParamResponse> listByProductId(Long productId);

    List<String> addOption(Long paramId, String option);
}


