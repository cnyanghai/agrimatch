package com.agrimatch.contract.mapper;

import com.agrimatch.contract.domain.BusContract;
import com.agrimatch.contract.dto.ContractQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractMapper {
    int insert(BusContract c);

    BusContract selectById(@Param("id") Long id);

    List<BusContract> selectList(@Param("q") ContractQuery q);

    int update(BusContract c);

    int logicalDelete(@Param("id") Long id, @Param("userId") Long userId);

    int updatePdfHash(@Param("id") Long id, @Param("userId") Long userId, @Param("pdfHash") String pdfHash);
}


