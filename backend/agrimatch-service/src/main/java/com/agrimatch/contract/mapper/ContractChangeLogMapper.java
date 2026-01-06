package com.agrimatch.contract.mapper;

import com.agrimatch.contract.domain.BusContractChangeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractChangeLogMapper {
    int insert(BusContractChangeLog log);

    List<BusContractChangeLog> selectByContractId(@Param("contractId") Long contractId);
}

