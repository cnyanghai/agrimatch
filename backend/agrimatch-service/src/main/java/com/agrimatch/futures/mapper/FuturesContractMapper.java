package com.agrimatch.futures.mapper;

import com.agrimatch.futures.domain.FuturesContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FuturesContractMapper {

    /**
     * 查询所有活跃合约
     */
    List<FuturesContract> selectActiveContracts(@Param("productCode") String productCode);

    /**
     * 根据合约代码查询
     */
    FuturesContract selectByContractCode(@Param("contractCode") String contractCode);

    /**
     * 批量查询合约价格
     */
    List<FuturesContract> selectByContractCodes(@Param("contractCodes") List<String> contractCodes);

    /**
     * 更新合约价格
     */
    int updatePrice(@Param("contractCode") String contractCode,
                    @Param("lastPrice") BigDecimal lastPrice,
                    @Param("prevClose") BigDecimal prevClose,
                    @Param("openPrice") BigDecimal openPrice,
                    @Param("highPrice") BigDecimal highPrice,
                    @Param("lowPrice") BigDecimal lowPrice,
                    @Param("volume") Long volume,
                    @Param("priceUpdateTime") LocalDateTime priceUpdateTime);

    /**
     * 查询所有产品品种
     */
    List<FuturesContract> selectDistinctProducts();

    /**
     * 插入或更新合约
     */
    int insert(FuturesContract contract);
}

