package com.agrimatch.deal.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.company.domain.BusCompany;
import com.agrimatch.company.mapper.CompanyMapper;
import com.agrimatch.deal.domain.BusDeal;
import com.agrimatch.deal.dto.DealCreateRequest;
import com.agrimatch.deal.dto.DealResponse;
import com.agrimatch.deal.mapper.DealMapper;
import com.agrimatch.deal.service.DealService;
import com.agrimatch.requirement.domain.BusRequirement;
import com.agrimatch.requirement.mapper.RequirementMapper;
import com.agrimatch.supply.domain.BusSupply;
import com.agrimatch.supply.mapper.SupplyMapper;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import com.agrimatch.util.GeoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class DealServiceImpl implements DealService {
    private final DealMapper dealMapper;
    private final RequirementMapper requirementMapper;
    private final SupplyMapper supplyMapper;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;

    @Value("${agrimatch.freight.rate-per-ton-km:0.8}")
    private BigDecimal freightRatePerTonKm;

    public DealServiceImpl(DealMapper dealMapper,
                           RequirementMapper requirementMapper,
                           SupplyMapper supplyMapper,
                           UserMapper userMapper,
                           CompanyMapper companyMapper) {
        this.dealMapper = dealMapper;
        this.requirementMapper = requirementMapper;
        this.supplyMapper = supplyMapper;
        this.userMapper = userMapper;
        this.companyMapper = companyMapper;
    }

    @Override
    public Long create(Long buyerUserId, DealCreateRequest req) {
        if (buyerUserId == null) throw new ApiException(401, "未登录");
        if (req == null || req.getRequirementId() == null || req.getSupplyId() == null || req.getQuantity() == null) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        if (req.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "成交数量必须大于 0");
        }

        SysUser buyer = userMapper.selectById(buyerUserId);
        if (buyer == null) throw new ApiException(401, "未登录");
        if (buyer.getCompanyId() == null) throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "请先完善公司档案（绑定公司）");

        BusRequirement r = requirementMapper.selectById(req.getRequirementId());
        if (r == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "需求不存在/已过期");
        if (!buyerUserId.equals(r.getUserId())) {
            throw new ApiException(403, "仅需求发布人可确认成交");
        }
        // 2=下架（兼容旧语义）
        if (r.getStatus() != null && r.getStatus() == 2) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "该需求已下架");
        }
        if (r.getQuantity() == null) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "该需求未填写总数量，暂不支持多次成交");
        }

        BusSupply s = supplyMapper.selectById(req.getSupplyId());
        if (s == null) throw new ApiException(ResultCode.NOT_FOUND.getCode(), "供应不存在");
        // 2=已下架，3=全部成交
        if (s.getStatus() != null && (s.getStatus() == 2 || s.getStatus() == 3)) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "该供应不可成交（已下架或已全部成交）");
        }
        if (s.getQuantity() == null) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "该供应未填写总数量，暂不支持多次成交");
        }
        if (StringUtils.hasText(r.getCategoryName()) && StringUtils.hasText(s.getCategoryName())) {
            if (!r.getCategoryName().trim().equalsIgnoreCase(s.getCategoryName().trim())) {
                throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "供应品类与需求品类不一致");
            }
        }

        // supply remaining quantity check (avoid oversell)
        BigDecimal sumS0 = dealMapper.sumConfirmedQuantityBySupplyId(s.getId());
        if (sumS0 == null) sumS0 = BigDecimal.ZERO;
        BigDecimal remainingS = s.getQuantity().subtract(sumS0);
        if (remainingS.compareTo(req.getQuantity()) < 0) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "成交数量超过供应剩余量，剩余：" + remainingS);
        }

        BigDecimal sum = dealMapper.sumConfirmedQuantityByRequirementId(r.getId());
        if (sum == null) sum = BigDecimal.ZERO;
        BigDecimal remaining = r.getQuantity().subtract(sum);
        if (remaining.compareTo(req.getQuantity()) < 0) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "成交数量超过剩余需求量，剩余：" + remaining);
        }

        BigDecimal finalEx = req.getFinalExFactoryPrice() != null ? req.getFinalExFactoryPrice() : s.getExFactoryPrice();
        if (finalEx == null || finalEx.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "成交出厂价不合法");
        }

        String deliveryMode = StringUtils.hasText(req.getDeliveryMode()) ? req.getDeliveryMode() : s.getDeliveryMode();

        BusDeal d = new BusDeal();
        d.setRequirementId(r.getId());
        d.setSupplyId(s.getId());
        d.setBuyerCompanyId(buyer.getCompanyId());
        d.setSellerCompanyId(s.getCompanyId());
        d.setBuyerUserId(buyerUserId);
        d.setSellerUserId(s.getUserId());
        d.setQuantity(req.getQuantity());
        d.setFinalExFactoryPrice(finalEx);
        d.setDeliveryMode(StringUtils.hasText(deliveryMode) ? deliveryMode : null);
        d.setStatus(1);

        // distance & delivered price (Beta)
        BusCompany buyerCompany = companyMapper.selectById(buyer.getCompanyId());
        BusCompany sellerCompany = companyMapper.selectById(s.getCompanyId());
        BigDecimal fromLat = buyerCompany != null ? buyerCompany.getLat() : null;
        BigDecimal fromLng = buyerCompany != null ? buyerCompany.getLng() : null;
        BigDecimal toLat = sellerCompany != null ? sellerCompany.getLat() : null;
        BigDecimal toLng = sellerCompany != null ? sellerCompany.getLng() : null;
        // prefer requirement purchase point
        if (r.getPurchaseLat() != null && r.getPurchaseLng() != null) {
            fromLat = r.getPurchaseLat();
            fromLng = r.getPurchaseLng();
        }
        BigDecimal km = GeoUtil.haversineKm(fromLat, fromLng, toLat, toLng);
        d.setDistanceKm(km);
        d.setFreightRatePerTonKm(freightRatePerTonKm);
        if (km != null && freightRatePerTonKm != null) {
            d.setDeliveredPrice(finalEx.add(km.multiply(freightRatePerTonKm)).setScale(2, RoundingMode.HALF_UP));
        }

        int rows = dealMapper.insert(d);
        if (rows != 1 || d.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);

        // update requirement status: 0发布, 1部分成交, 2下架, 3全部成交
        BigDecimal sum2 = dealMapper.sumConfirmedQuantityByRequirementId(r.getId());
        if (sum2 == null) sum2 = BigDecimal.ZERO;
        int newStatus = sum2.compareTo(r.getQuantity()) >= 0 ? 3 : 1;
        BusRequirement up = new BusRequirement();
        up.setId(r.getId());
        up.setUserId(buyerUserId);
        up.setStatus(newStatus);
        requirementMapper.update(up);

        // update supply status: 0发布中, 1部分成交, 2下架, 3全部成交
        BigDecimal sumS = dealMapper.sumConfirmedQuantityBySupplyId(s.getId());
        if (sumS == null) sumS = BigDecimal.ZERO;
        // supply.quantity 已在上方保证非空
        int newSupplyStatus = sumS.compareTo(s.getQuantity()) >= 0 ? 3 : 1;
        BusSupply supUp = new BusSupply();
        supUp.setId(s.getId());
        supUp.setUserId(s.getUserId());
        supUp.setStatus(newSupplyStatus);
        supplyMapper.update(supUp);

        return d.getId();
    }

    @Override
    public DealResponse getById(Long id) {
        BusDeal d = dealMapper.selectById(id);
        if (d == null) throw new ApiException(ResultCode.NOT_FOUND);
        return toResponse(d);
    }

    @Override
    public List<DealResponse> listByRequirement(Long requirementId) {
        if (requirementId == null) throw new ApiException(ResultCode.PARAM_ERROR);
        List<BusDeal> list = dealMapper.selectByRequirementId(requirementId);
        List<DealResponse> out = new ArrayList<>();
        for (BusDeal d : list) out.add(toResponse(d));
        return out;
    }

    private static DealResponse toResponse(BusDeal d) {
        DealResponse r = new DealResponse();
        r.setId(d.getId());
        r.setRequirementId(d.getRequirementId());
        r.setSupplyId(d.getSupplyId());
        r.setBuyerCompanyId(d.getBuyerCompanyId());
        r.setSellerCompanyId(d.getSellerCompanyId());
        r.setBuyerUserId(d.getBuyerUserId());
        r.setSellerUserId(d.getSellerUserId());
        r.setQuantity(d.getQuantity());
        r.setFinalExFactoryPrice(d.getFinalExFactoryPrice());
        r.setDeliveryMode(d.getDeliveryMode());
        r.setDistanceKm(d.getDistanceKm());
        r.setFreightRatePerTonKm(d.getFreightRatePerTonKm());
        r.setDeliveredPrice(d.getDeliveredPrice());
        r.setStatus(d.getStatus());
        r.setCreateTime(d.getCreateTime());
        return r;
    }
}


