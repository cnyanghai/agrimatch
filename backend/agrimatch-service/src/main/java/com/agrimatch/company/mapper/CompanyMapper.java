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

    List<com.agrimatch.company.dto.CompanyCardResponse> selectTopSuppliers(@Param("limit") int limit, @Param("region") String region);

    List<com.agrimatch.company.dto.CompanyCardResponse> selectTopBuyers(@Param("limit") int limit, @Param("categoryName") String categoryName);

    List<com.agrimatch.company.dto.CompanyCardResponse> selectTopCompanies(@Param("type") String type, @Param("limit") int limit);

    List<com.agrimatch.company.dto.CompanyCardResponse> selectDirectory(@Param("type") String type, @Param("letter") String letter, @Param("offset") int offset, @Param("limit") int limit);

    long countDirectory(@Param("type") String type, @Param("letter") String letter);

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


