package com.agrimatch.product_param.mapper;

import com.agrimatch.product_param.domain.NhtProductParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductParamMapper {
    List<NhtProductParam> selectByProductId(@Param("productId") Long productId);

    NhtProductParam selectById(@Param("id") Long id);

    int updateOptions(@Param("id") Long id, @Param("options") String options);
}


