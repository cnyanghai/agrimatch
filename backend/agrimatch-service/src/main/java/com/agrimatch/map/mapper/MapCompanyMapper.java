package com.agrimatch.map.mapper;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface MapCompanyMapper {
    List<Row> selectCompanyMarkers(@Param("keyword") String keyword);

    class Row {
        private Long companyId;
        private String companyName;
        private String address;
        private BigDecimal lat;
        private BigDecimal lng;
        private Integer supplyCount;
        private Integer requirementCount;
        private String supplyCats;
        private String requirementCats;
        private LocalDateTime updateTime;

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

        public String getSupplyCats() {
            return supplyCats;
        }

        public void setSupplyCats(String supplyCats) {
            this.supplyCats = supplyCats;
        }

        public String getRequirementCats() {
            return requirementCats;
        }

        public void setRequirementCats(String requirementCats) {
            this.requirementCats = requirementCats;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }
    }
}





