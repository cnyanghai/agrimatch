package com.agrimatch.supply.mapper;

import com.agrimatch.supply.domain.BusSupplyBasis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SupplyBasisMapper {

    /**
     * 插入基差报价
     */
    int insert(BusSupplyBasis basis);

    /**
     * 批量插入基差报价
     */
    int batchInsert(@Param("list") List<BusSupplyBasis> list);

    /**
     * 根据供应ID查询基差报价列表（关联期货合约获取最新价格）
     */
    List<BusSupplyBasis> selectBySupplyId(@Param("supplyId") Long supplyId);

    /**
     * 根据供应ID批量查询基差报价
     */
    List<BusSupplyBasis> selectBySupplyIds(@Param("supplyIds") List<Long> supplyIds);

    /**
     * 根据供应ID删除基差报价（逻辑删除）
     */
    int deleteBySupplyId(@Param("supplyId") Long supplyId);

    /**
     * 更新已售量
     */
    int updateSoldQty(@Param("id") Long id, @Param("soldQty") java.math.BigDecimal soldQty);
}

