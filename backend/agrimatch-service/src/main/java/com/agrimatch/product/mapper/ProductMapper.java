package com.agrimatch.product.mapper;

import com.agrimatch.product.domain.NhtProduct;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    List<NhtProduct> selectAllActive();

    List<NhtProduct> search(@Param("keyword") String keyword);

    int insertCustom(NhtProduct p);
}


