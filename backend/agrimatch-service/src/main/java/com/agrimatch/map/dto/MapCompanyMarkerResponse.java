package com.agrimatch.map.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MapCompanyMarkerResponse {
    private Long companyId;
    private String companyName;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;

    private Integer supplyCount;
    private Integer requirementCount;

    private List<String> supplyCategories = new ArrayList<>();
    private List<String> requirementCategories = new ArrayList<>();

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }

    public Integer getSupplyCount() {
        return supplyCount;
    }

    public void setSupplyCount(Integer supplyCount) {
        this.supplyCount = supplyCount;
    }

    public Integer getRequirementCount() {
        return requirementCount;
    }

    public void setRequirementCount(Integer requirementCount) {
        this.requirementCount = requirementCount;
    }

    public List<String> getSupplyCategories() {
        return supplyCategories;
    }

    public void setSupplyCategories(List<String> supplyCategories) {
        this.supplyCategories = supplyCategories;
    }

    public List<String> getRequirementCategories() {
        return requirementCategories;
    }

    public void setRequirementCategories(List<String> requirementCategories) {
        this.requirementCategories = requirementCategories;
    }
}





