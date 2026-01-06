package com.agrimatch.company.mapper;

import com.agrimatch.company.domain.BusCompany;
import com.agrimatch.company.dto.CompanyBriefResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CompanyMapper {
    int insert(BusCompany c);

    BusCompany selectById(@Param("id") Long id);

    BusCompany selectByOwnerUserId(@Param("ownerUserId") Long ownerUserId);

    int update(BusCompany c);

    List<IdNameRow> selectNamesByIds(@Param("ids") List<Long> ids);

    List<CompanyBriefResponse> search(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /** 查询有地址但缺少坐标的公司（用于启动时自动补全） */
    List<BusCompany> selectMissingCoords();

    /** 仅更新坐标（内部使用，不校验 ownerUserId） */
    int updateCoords(@Param("id") Long id, @Param("lat") java.math.BigDecimal lat, @Param("lng") java.math.BigDecimal lng);

    class IdNameRow {
        private Long id;
        private String companyName;

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
    }
}


