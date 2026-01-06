package com.agrimatch.contract.mapper;

import com.agrimatch.contract.domain.BusContractSignature;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractSignatureMapper {
    int insert(BusContractSignature signature);

    List<BusContractSignature> selectByContractId(@Param("contractId") Long contractId);

    BusContractSignature selectByContractAndParty(@Param("contractId") Long contractId, @Param("partyType") String partyType);

    int countByContractId(@Param("contractId") Long contractId);
}

