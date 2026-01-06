package com.agrimatch.contract.mapper;

import com.agrimatch.contract.domain.BusContractMilestone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ContractMilestoneMapper {
    int insert(BusContractMilestone milestone);

    BusContractMilestone selectById(@Param("id") Long id);

    List<BusContractMilestone> selectByContractId(@Param("contractId") Long contractId);

    int submit(@Param("id") Long id, 
               @Param("operatorUserId") Long operatorUserId,
               @Param("actualDate") LocalDate actualDate,
               @Param("evidenceUrl") String evidenceUrl,
               @Param("evidenceJson") String evidenceJson,
               @Param("remark") String remark);

    int confirm(@Param("id") Long id, @Param("confirmUserId") Long confirmUserId);

    int reject(@Param("id") Long id, @Param("confirmUserId") Long confirmUserId, @Param("remark") String remark);

    int logicalDelete(@Param("id") Long id);

    int countPendingByContractId(@Param("contractId") Long contractId);
}

