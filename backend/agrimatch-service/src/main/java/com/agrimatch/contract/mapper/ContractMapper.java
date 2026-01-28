package com.agrimatch.contract.mapper;

import com.agrimatch.contract.domain.BusContract;
import com.agrimatch.contract.dto.ContractQuery;
import com.agrimatch.contract.dto.PartnerCompanyResponse;
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

    /** 统计已签订合同数（status >= 2） */
    Long countSignedContracts(@Param("companyId") Long companyId);

    /** 统计合作商户数 */
    Long countPartnerCompanies(@Param("companyId") Long companyId);

    /** 获取合作商家列表 */
    List<PartnerCompanyResponse> selectPartnerCompanies(@Param("companyId") Long companyId);
}
