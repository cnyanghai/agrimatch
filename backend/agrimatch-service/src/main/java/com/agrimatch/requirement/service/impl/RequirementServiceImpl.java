package com.agrimatch.requirement.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.company.domain.BusCompany;
import com.agrimatch.company.mapper.CompanyMapper;
import com.agrimatch.deal.mapper.DealMapper;
import com.agrimatch.requirement.domain.BusRequirement;
import com.agrimatch.requirement.dto.RequirementCreateRequest;
import com.agrimatch.requirement.dto.RequirementQuery;
import com.agrimatch.requirement.dto.RequirementResponse;
import com.agrimatch.requirement.dto.RequirementUpdateRequest;
import com.agrimatch.requirement.mapper.RequirementMapper;
import com.agrimatch.requirement.service.RequirementService;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import com.agrimatch.util.GeoUtil;
import com.agrimatch.util.NoUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RequirementServiceImpl implements RequirementService {
    private final RequirementMapper requirementMapper;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;
    private final DealMapper dealMapper;

    public RequirementServiceImpl(RequirementMapper requirementMapper, UserMapper userMapper, CompanyMapper companyMapper, DealMapper dealMapper) {
        this.requirementMapper = requirementMapper;
        this.userMapper = userMapper;
        this.companyMapper = companyMapper;
        this.dealMapper = dealMapper;
    }

    @Override
    public Long create(Long userId, RequirementCreateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        SysUser u = userMapper.selectById(userId);
        if (u == null) throw new ApiException(401, "未登录");
        if (u.getCompanyId() == null) throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "请先完善公司档案（绑定公司）");

        BusRequirement r = new BusRequirement();
        r.setCompanyId(u.getCompanyId());
        r.setUserId(userId);
        r.setCategoryName(req.getCategoryName());
        r.setContractNo(StringUtils.hasText(req.getContractNo()) ? req.getContractNo().trim() : NoUtil.gen("CG"));
        r.setQuantity(req.getQuantity());
        r.setPackaging(req.getPackaging());
        r.setExpectedPrice(req.getExpectedPrice());
        r.setInvoiceType(StringUtils.hasText(req.getInvoiceType()) ? req.getInvoiceType().trim() : null);
        r.setPaymentMethod(req.getPaymentMethod());
        r.setDeliveryMethod(StringUtils.hasText(req.getDeliveryMethod()) ? req.getDeliveryMethod().trim() : null);
        r.setParamsJson(req.getParamsJson());
        r.setRemark(StringUtils.hasText(req.getRemark()) ? req.getRemark().trim() : null);
        r.setExpireMinutes(normalizeExpireMinutes(req.getExpireMinutes()));
        if (r.getExpireMinutes() != null) {
            r.setExpireTime(LocalDateTime.now().plusMinutes(r.getExpireMinutes()));
        }
        // 地址：默认公司地址，可被用户覆盖
        String addr = emptyToNull(req.getPurchaseAddress());
        if (addr == null) {
            BusCompany c = companyMapper.selectById(u.getCompanyId());
            if (c != null && StringUtils.hasText(c.getAddress())) {
                addr = c.getAddress();
            }
        }
        r.setPurchaseAddress(addr);
        r.setStatus(0);

        int rows = requirementMapper.insert(r);
        if (rows != 1 || r.getId() == null) {
            throw new ApiException(ResultCode.SERVER_ERROR);
        }
        return r.getId();
    }

    @Override
    public RequirementResponse getById(Long id) {
        BusRequirement r = requirementMapper.selectById(id);
        if (r == null) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        return toResponse(r);
    }

    @Override
    public List<RequirementResponse> list(Long viewerUserId, RequirementQuery query) {
        // 允许匿名访问（大厅页面）
        normalizeQuery(query);
        List<BusRequirement> list = requirementMapper.selectList(query);

        // viewer company coords (仅登录用户可用)
        BusCompany viewerCompany = null;
        if (viewerUserId != null) {
            SysUser viewer = userMapper.selectById(viewerUserId);
            if (viewer != null && viewer.getCompanyId() != null) {
                viewerCompany = companyMapper.selectById(viewer.getCompanyId());
            }
        }

        List<RequirementResponse> out = new ArrayList<>();
        for (BusRequirement r : list) {
            RequirementResponse resp = toResponse(r);

            // remaining quantity
            if (r.getQuantity() != null) {
                BigDecimal sum = dealMapper.sumConfirmedQuantityByRequirementId(r.getId());
                if (sum == null) sum = BigDecimal.ZERO;
                resp.setRemainingQuantity(r.getQuantity().subtract(sum));
            }

            // distance (Beta): viewer company -> purchase point (or buyer company)
            if (viewerCompany != null && viewerCompany.getLat() != null && viewerCompany.getLng() != null) {
                BigDecimal toLat = r.getPurchaseLat();
                BigDecimal toLng = r.getPurchaseLng();
                if (toLat == null || toLng == null) {
                    BusCompany buyerCompany = companyMapper.selectById(r.getCompanyId());
                    if (buyerCompany != null) {
                        toLat = buyerCompany.getLat();
                        toLng = buyerCompany.getLng();
                    }
                }
                if (toLat != null && toLng != null) {
                    resp.setDistanceKm(GeoUtil.haversineKm(viewerCompany.getLat(), viewerCompany.getLng(), toLat, toLng));
                }
            }

            out.add(resp);
        }

        // in-memory sort for distance (Beta)
        if (query != null && "distance".equalsIgnoreCase(query.getOrderBy())) {
            boolean asc = "asc".equalsIgnoreCase(query.getOrder());
            Comparator<RequirementResponse> c = Comparator.comparing(RequirementResponse::getDistanceKm, Comparator.nullsLast(BigDecimal::compareTo));
            out.sort(asc ? c : c.reversed());
        }

        return out;
    }

    @Override
    public void update(Long userId, Long id, RequirementUpdateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        BusRequirement r = new BusRequirement();
        r.setId(id);
        r.setUserId(userId);
        r.setCategoryName(emptyToNull(req.getCategoryName()));
        r.setQuantity(req.getQuantity());
        r.setExpectedPrice(req.getExpectedPrice());
        r.setPackaging(emptyToNull(req.getPackaging()));
        r.setInvoiceType(emptyToNull(req.getInvoiceType()));
        r.setPaymentMethod(emptyToNull(req.getPaymentMethod()));
        r.setDeliveryMethod(emptyToNull(req.getDeliveryMethod()));
        r.setParamsJson(emptyToNull(req.getParamsJson()));
        r.setRemark(emptyToNull(req.getRemark()));

        // expire: minutes -> expireTime（管理端可重置有效期/再次发布）
        if (req.getExpireMinutes() != null) {
            Integer m = normalizeExpireMinutes(req.getExpireMinutes());
            r.setExpireMinutes(m);
            if (m == null) {
                r.setExpireTime(null);
            } else {
                r.setExpireTime(LocalDateTime.now().plusMinutes(m));
            }
        }
        r.setPurchaseLat(req.getPurchaseLat());
        r.setPurchaseLng(req.getPurchaseLng());
        r.setPurchaseAddress(emptyToNull(req.getPurchaseAddress()));
        r.setStatus(req.getStatus());

        int rows = requirementMapper.update(r);
        if (rows != 1) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
    }

    @Override
    public void delete(Long userId, Long id) {
        if (userId == null) throw new ApiException(401, "未登录");
        int rows = requirementMapper.logicalDelete(id, userId);
        if (rows != 1) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
    }

    private RequirementResponse toResponse(BusRequirement r) {
        RequirementResponse o = new RequirementResponse();
        o.setId(r.getId());
        o.setCompanyId(r.getCompanyId());
        o.setUserId(r.getUserId());
        o.setCompanyName(r.getCompanyName());
        o.setUserName(r.getUserName());
        o.setNickName(r.getNickName());
        o.setCategoryName(r.getCategoryName());
        o.setContractNo(r.getContractNo());
        o.setQuantity(r.getQuantity());
        o.setExpectedPrice(r.getExpectedPrice());
        o.setPackaging(r.getPackaging());
        o.setInvoiceType(r.getInvoiceType());
        o.setPaymentMethod(r.getPaymentMethod());
        o.setDeliveryMethod(r.getDeliveryMethod());
        o.setParamsJson(r.getParamsJson());
        o.setRemark(r.getRemark());
        o.setExpireMinutes(r.getExpireMinutes());
        o.setExpireTime(r.getExpireTime());
        o.setPurchaseLat(r.getPurchaseLat());
        o.setPurchaseLng(r.getPurchaseLng());
        o.setPurchaseAddress(r.getPurchaseAddress());
        o.setStatus(r.getStatus());
        // remainingQuantity/distanceKm are computed in list()
        o.setCreateTime(r.getCreateTime());
        o.setUpdateTime(r.getUpdateTime());
        return o;
    }

    private static void normalizeQuery(RequirementQuery q) {
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

    private static Integer normalizeExpireMinutes(Integer minutes) {
        if (minutes == null) return null;
        if (minutes <= 0) return null;
        // 防止过大（最多 7 天）
        return Math.min(minutes, 7 * 24 * 60);
    }
}


