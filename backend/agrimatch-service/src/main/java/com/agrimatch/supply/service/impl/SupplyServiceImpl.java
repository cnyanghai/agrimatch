package com.agrimatch.supply.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.company.domain.BusCompany;
import com.agrimatch.company.mapper.CompanyMapper;
import com.agrimatch.deal.mapper.DealMapper;
import com.agrimatch.supply.domain.BusSupply;
import com.agrimatch.supply.dto.SupplyCreateRequest;
import com.agrimatch.supply.dto.SupplyQuery;
import com.agrimatch.supply.dto.SupplyResponse;
import com.agrimatch.supply.dto.SupplyUpdateRequest;
import com.agrimatch.supply.mapper.SupplyMapper;
import com.agrimatch.supply.service.SupplyService;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import com.agrimatch.util.GeoUtil;
import com.agrimatch.util.NoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SupplyServiceImpl implements SupplyService {
    private final SupplyMapper supplyMapper;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;
    private final DealMapper dealMapper;

    @Value("${agrimatch.freight.rate-per-ton-km:0.8}")
    private BigDecimal freightRatePerTonKm;

    public SupplyServiceImpl(SupplyMapper supplyMapper, UserMapper userMapper, CompanyMapper companyMapper, DealMapper dealMapper) {
        this.supplyMapper = supplyMapper;
        this.userMapper = userMapper;
        this.companyMapper = companyMapper;
        this.dealMapper = dealMapper;
    }

    @Override
    public Long create(Long userId, SupplyCreateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        SysUser u = userMapper.selectById(userId);
        if (u == null) throw new ApiException(401, "未登录");
        if (u.getCompanyId() == null) throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "请先完善公司档案（绑定公司）");

        BusSupply s = new BusSupply();
        s.setCompanyId(u.getCompanyId());
        s.setUserId(userId);
        s.setCategoryName(req.getCategoryName());
        s.setSupplyNo(StringUtils.hasText(req.getSupplyNo()) ? req.getSupplyNo().trim() : NoUtil.gen("GY"));
        s.setOrigin(StringUtils.hasText(req.getOrigin()) ? req.getOrigin().trim() : null);
        s.setQuantity(req.getQuantity());
        s.setExFactoryPrice(req.getExFactoryPrice());
        // 地址：默认公司地址，可被用户覆盖
        String addr = emptyToNull(req.getShipAddress());
        if (addr == null) {
            BusCompany c = companyMapper.selectById(u.getCompanyId());
            if (c != null && StringUtils.hasText(c.getAddress())) {
                addr = c.getAddress();
            }
        }
        s.setShipAddress(addr);
        s.setDeliveryMode(req.getDeliveryMode());
        s.setPackaging(emptyToNull(req.getPackaging()));
        s.setStorageMethod(emptyToNull(req.getStorageMethod()));
        s.setPriceRulesJson(req.getPriceRulesJson());
        s.setParamsJson(req.getParamsJson());
        s.setRemark(StringUtils.hasText(req.getRemark()) ? req.getRemark().trim() : null);
        s.setExpireMinutes(normalizeExpireMinutes(req.getExpireMinutes()));
        if (s.getExpireMinutes() != null) {
            s.setExpireTime(LocalDateTime.now().plusMinutes(s.getExpireMinutes()));
        }
        s.setStatus(0);

        int rows = supplyMapper.insert(s);
        if (rows != 1 || s.getId() == null) {
            throw new ApiException(ResultCode.SERVER_ERROR);
        }
        return s.getId();
    }

    @Override
    public SupplyResponse getById(Long id) {
        BusSupply s = supplyMapper.selectById(id);
        if (s == null) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        return toResponse(s);
    }

    @Override
    public List<SupplyResponse> list(Long viewerUserId, SupplyQuery query) {
        if (viewerUserId == null) throw new ApiException(401, "未登录");
        normalizeQuery(query);
        // 过期自动撤下（status=2）
        supplyMapper.expireDownShelf();
        List<BusSupply> list = supplyMapper.selectList(query);

        // viewer company coords
        BusCompany viewerCompany = null;
        SysUser viewer = userMapper.selectById(viewerUserId);
        if (viewer != null && viewer.getCompanyId() != null) {
            viewerCompany = companyMapper.selectById(viewer.getCompanyId());
        }

        List<SupplyResponse> out = new ArrayList<>();
        for (BusSupply s : list) {
            SupplyResponse r = toResponse(s);

            // remaining quantity（用于管理端/成交态展示）
            if (s.getQuantity() != null) {
                BigDecimal sum = dealMapper.sumConfirmedQuantityBySupplyId(s.getId());
                if (sum == null) sum = BigDecimal.ZERO;
                r.setRemainingQuantity(s.getQuantity().subtract(sum));
            }

            // compute distance & delivered price (Beta)
            if (viewerCompany != null && viewerCompany.getLat() != null && viewerCompany.getLng() != null) {
                BusCompany sc = companyMapper.selectById(s.getCompanyId());
                if (sc != null && sc.getLat() != null && sc.getLng() != null) {
                    BigDecimal km = GeoUtil.haversineKm(viewerCompany.getLat(), viewerCompany.getLng(), sc.getLat(), sc.getLng());
                    r.setDistanceKm(km);
                    if (km != null && r.getExFactoryPrice() != null && freightRatePerTonKm != null) {
                        BigDecimal delivered = r.getExFactoryPrice().add(km.multiply(freightRatePerTonKm))
                                .setScale(2, RoundingMode.HALF_UP);
                        r.setDeliveredPrice(delivered);
                    }
                }
            }
            out.add(r);
        }

        // in-memory sort for Beta orderBy
        if (query != null && StringUtils.hasText(query.getOrderBy())) {
            String ob = query.getOrderBy();
            boolean asc = "asc".equalsIgnoreCase(query.getOrder());
            if ("distance".equalsIgnoreCase(ob)) {
                Comparator<SupplyResponse> c = Comparator.comparing(SupplyResponse::getDistanceKm, Comparator.nullsLast(BigDecimal::compareTo));
                out.sort(asc ? c : c.reversed());
            } else if ("delivered_price".equalsIgnoreCase(ob)) {
                Comparator<SupplyResponse> c = Comparator.comparing(SupplyResponse::getDeliveredPrice, Comparator.nullsLast(BigDecimal::compareTo));
                out.sort(asc ? c : c.reversed());
            }
        }

        return out;
    }

    @Override
    public void update(Long userId, Long id, SupplyUpdateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        BusSupply s = new BusSupply();
        s.setId(id);
        s.setUserId(userId);
        s.setCategoryName(emptyToNull(req.getCategoryName()));
        s.setOrigin(emptyToNull(req.getOrigin()));
        s.setQuantity(req.getQuantity());
        s.setExFactoryPrice(req.getExFactoryPrice());
        s.setShipAddress(emptyToNull(req.getShipAddress()));
        s.setDeliveryMode(emptyToNull(req.getDeliveryMode()));
        s.setPackaging(emptyToNull(req.getPackaging()));
        s.setStorageMethod(emptyToNull(req.getStorageMethod()));
        s.setPriceRulesJson(emptyToNull(req.getPriceRulesJson()));
        s.setParamsJson(emptyToNull(req.getParamsJson()));
        s.setRemark(emptyToNull(req.getRemark()));

        // expire: minutes -> expireTime（管理端可重置有效期/再次发布）
        if (req.getExpireMinutes() != null) {
            Integer m = normalizeExpireMinutes(req.getExpireMinutes());
            s.setExpireMinutes(m);
            if (m == null) {
                s.setExpireTime(null);
            } else {
                s.setExpireTime(LocalDateTime.now().plusMinutes(m));
            }
        }
        s.setStatus(req.getStatus());

        int rows = supplyMapper.update(s);
        if (rows != 1) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
    }

    @Override
    public void delete(Long userId, Long id) {
        if (userId == null) throw new ApiException(401, "未登录");
        int rows = supplyMapper.logicalDelete(id, userId);
        if (rows != 1) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
    }

    private static SupplyResponse toResponse(BusSupply s) {
        SupplyResponse o = new SupplyResponse();
        o.setId(s.getId());
        o.setCompanyId(s.getCompanyId());
        o.setUserId(s.getUserId());
        o.setCompanyName(s.getCompanyName());
        o.setUserName(s.getUserName());
        o.setNickName(s.getNickName());
        o.setCategoryName(s.getCategoryName());
        o.setSupplyNo(s.getSupplyNo());
        o.setOrigin(s.getOrigin());
        o.setQuantity(s.getQuantity());
        o.setExFactoryPrice(s.getExFactoryPrice());
        o.setShipAddress(s.getShipAddress());
        o.setDeliveryMode(s.getDeliveryMode());
        o.setPackaging(s.getPackaging());
        o.setStorageMethod(s.getStorageMethod());
        o.setPriceRulesJson(s.getPriceRulesJson());
        o.setParamsJson(s.getParamsJson());
        o.setRemark(s.getRemark());
        o.setExpireMinutes(s.getExpireMinutes());
        o.setExpireTime(s.getExpireTime());
        o.setStatus(s.getStatus());
        o.setCreateTime(s.getCreateTime());
        o.setUpdateTime(s.getUpdateTime());
        return o;
    }

    private static Integer normalizeExpireMinutes(Integer minutes) {
        if (minutes == null) return null;
        if (minutes <= 0) return null;
        // 防止过大（最多 7 天）
        return Math.min(minutes, 7 * 24 * 60);
    }

    private static void normalizeQuery(SupplyQuery q) {
        if (q == null) return;
        if (!StringUtils.hasText(q.getOrderBy())) {
            q.setOrderBy("create_time");
        }
        if (!StringUtils.hasText(q.getOrder())) {
            q.setOrder("desc");
        } else {
            q.setOrder(q.getOrder().toLowerCase());
        }
    }

    private static String emptyToNull(String s) {
        return StringUtils.hasText(s) ? s : null;
    }
}


