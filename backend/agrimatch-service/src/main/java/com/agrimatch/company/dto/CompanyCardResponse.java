package com.agrimatch.company.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CompanyCardResponse {
    private Long id;
    private String companyName;
    private String province;
    private String city;
    private Long count; // 供应/需求数量
    private String categoryNamesStr; // 数据库联接后的逗号分隔字符串
    private List<String> categoryNames;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getCategoryNamesStr() {
        return categoryNamesStr;
    }

    public void setCategoryNamesStr(String categoryNamesStr) {
        this.categoryNamesStr = categoryNamesStr;
        if (categoryNamesStr != null) {
            this.categoryNames = Arrays.stream(categoryNamesStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            this.categoryNames = Collections.emptyList();
        }
    }

    public List<String> getCategoryNames() {
        return categoryNames;
    }

    public void setCategoryNames(List<String> categoryNames) {
        this.categoryNames = categoryNames;
    }
}
