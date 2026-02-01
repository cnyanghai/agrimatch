package com.agrimatch.follow.service.impl;

import com.agrimatch.follow.domain.BusUserFollow;
import com.agrimatch.follow.dto.FollowedUserResponse;
import com.agrimatch.follow.mapper.FollowMapper;
import com.agrimatch.follow.service.FollowService;
import com.agrimatch.requirement.dto.RequirementResponse;
import com.agrimatch.supply.domain.BusSupplyBasis;
import com.agrimatch.supply.dto.BasisQuoteResponse;
import com.agrimatch.supply.dto.SupplyResponse;
import com.agrimatch.supply.mapper.SupplyBasisMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final SupplyBasisMapper supplyBasisMapper;

    public FollowServiceImpl(FollowMapper followMapper, SupplyBasisMapper supplyBasisMapper) {
        this.followMapper = followMapper;
        this.supplyBasisMapper = supplyBasisMapper;
    }

    @Override
    @Transactional
    public void follow(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("不能关注自己");
        }

        // 检查是否已关注
        BusUserFollow existing = followMapper.selectByUserAndFollowUser(userId, targetUserId);
        if (existing != null) {
            return; // 已关注，忽略
        }

        BusUserFollow follow = new BusUserFollow();
        follow.setUserId(userId);
        follow.setFollowUserId(targetUserId);
        followMapper.insert(follow);
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long targetUserId) {
        followMapper.deleteByUserAndFollowUser(userId, targetUserId);
    }

    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        BusUserFollow follow = followMapper.selectByUserAndFollowUser(userId, targetUserId);
        return follow != null;
    }

    @Override
    public List<FollowedUserResponse> getFollowedUsers(Long userId) {
        return followMapper.selectFollowedUsers(userId);
    }

    @Override
    public List<RequirementResponse> getFollowedRequirements(Long userId) {
        return followMapper.selectFollowedRequirements(userId);
    }

    @Override
    public List<SupplyResponse> getFollowedSupplies(Long userId) {
        List<SupplyResponse> supplies = followMapper.selectFollowedSupplies(userId);
        if (supplies.isEmpty()) return supplies;

        // 批量加载基差报价明细
        List<Long> basisSupplyIds = supplies.stream()
                .filter(s -> s.getPriceType() != null && s.getPriceType() == 1)
                .map(SupplyResponse::getId)
                .collect(Collectors.toList());

        if (!basisSupplyIds.isEmpty()) {
            List<BusSupplyBasis> allBasis = supplyBasisMapper.selectBySupplyIds(basisSupplyIds);
            Map<Long, List<BasisQuoteResponse>> basisMap = new HashMap<>();
            for (BusSupplyBasis basis : allBasis) {
                basisMap.computeIfAbsent(basis.getSupplyId(), k -> new ArrayList<>())
                        .add(toBasisQuoteResponse(basis));
            }
            for (SupplyResponse s : supplies) {
                if (s.getPriceType() != null && s.getPriceType() == 1) {
                    s.setBasisQuotes(basisMap.getOrDefault(s.getId(), new ArrayList<>()));
                }
            }
        }

        return supplies;
    }

    private static BasisQuoteResponse toBasisQuoteResponse(BusSupplyBasis basis) {
        BasisQuoteResponse r = new BasisQuoteResponse();
        r.setId(basis.getId());
        r.setContractCode(basis.getContractCode());
        r.setContractName(basis.getContractName());
        r.setBasisPrice(basis.getBasisPrice());
        r.setAvailableQty(basis.getAvailableQty());
        r.setSoldQty(basis.getSoldQty() != null ? basis.getSoldQty() : BigDecimal.ZERO);
        BigDecimal remaining = basis.getAvailableQty();
        if (basis.getSoldQty() != null) {
            remaining = remaining.subtract(basis.getSoldQty());
        }
        r.setRemainingQty(remaining);
        r.setLastPrice(basis.getLastPrice());
        r.setReferencePrice(basis.getReferencePrice());
        r.setRemark(basis.getRemark());
        return r;
    }

    @Override
    public int getFollowerCount(Long userId) {
        return followMapper.countFollowers(userId);
    }

    @Override
    public int getFollowingCount(Long userId) {
        return followMapper.countFollowing(userId);
    }
}

