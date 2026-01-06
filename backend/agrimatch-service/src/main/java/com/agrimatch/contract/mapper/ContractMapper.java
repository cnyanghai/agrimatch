package com.agrimatch.contract.mapper;

import com.agrimatch.contract.domain.BusContract;
import com.agrimatch.contract.dto.ContractQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ContractMapper {
    int insert(BusContract c);

    BusContract selectById(@Param("id") Long id);
    
    /** 查询合同详情，关联公司信息 */
    Map<String, Object> selectDetailById(@Param("id") Long id);
    
    BusContract selectByQuoteMessageId(@Param("quoteMessageId") Long quoteMessageId);

    List<BusContract> selectList(@Param("q") ContractQuery q);

    int update(BusContract c);
    
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int logicalDelete(@Param("id") Long id);
    
    String selectMaxContractNoForToday(@Param("datePrefix") String datePrefix);
}
