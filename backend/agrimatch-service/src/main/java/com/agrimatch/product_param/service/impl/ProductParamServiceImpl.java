package com.agrimatch.product_param.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.product_param.domain.NhtProductParam;
import com.agrimatch.product_param.dto.ProductParamResponse;
import com.agrimatch.product_param.mapper.ProductParamMapper;
import com.agrimatch.product_param.service.ProductParamService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductParamServiceImpl implements ProductParamService {
    private final ProductParamMapper mapper;

    public ProductParamServiceImpl(ProductParamMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ProductParamResponse> listByProductId(Long productId) {
        if (productId == null) throw new ApiException(ResultCode.PARAM_ERROR);
        List<NhtProductParam> list = mapper.selectByProductId(productId);
        List<ProductParamResponse> out = new ArrayList<>();
        for (NhtProductParam p : list) {
            ProductParamResponse r = new ProductParamResponse();
            r.setId(p.getId());
            r.setProductId(p.getProductId());
            r.setParamName(p.getParamName());
            r.setParamType(p.getParamType());
            r.setRequired("Y".equalsIgnoreCase(p.getRequired()));
            r.setSort(p.getSort());
            r.setOptions(parseOptions(p.getOptions()));
            out.add(r);
        }
        return out;
    }

    @Override
    public List<String> addOption(Long paramId, String option) {
        if (paramId == null || !StringUtils.hasText(option)) throw new ApiException(ResultCode.PARAM_ERROR);
        String v = option.trim();
        if (v.length() > 64) throw new ApiException(400, "选项内容过长");

        NhtProductParam p = mapper.selectById(paramId);
        if (p == null) throw new ApiException(ResultCode.NOT_FOUND);
        if (p.getParamType() == null || p.getParamType() != 1) {
            throw new ApiException(400, "该参数不是下拉类型，无法追加选项");
        }

        Set<String> set = new LinkedHashSet<>(parseOptions(p.getOptions()));
        set.add(v);
        String merged = String.join(",", set);
        if (merged.length() > 255) throw new ApiException(400, "选项总长度超限，请联系管理员配置");

        int rows = mapper.updateOptions(paramId, merged);
        if (rows != 1) throw new ApiException(ResultCode.SERVER_ERROR);
        return new ArrayList<>(set);
    }

    private static List<String> parseOptions(String options) {
        if (!StringUtils.hasText(options)) return new ArrayList<>();
        if ("0".equals(options.trim())) return new ArrayList<>();
        return Arrays.stream(options.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}


