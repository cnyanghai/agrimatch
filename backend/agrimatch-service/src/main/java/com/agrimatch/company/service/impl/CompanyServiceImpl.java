package com.agrimatch.company.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.company.domain.BusCompany;
import com.agrimatch.company.dto.CompanyBriefResponse;
import com.agrimatch.company.dto.CompanyCreateRequest;
import com.agrimatch.company.dto.CompanyResponse;
import com.agrimatch.company.dto.CompanyUpdateRequest;
import com.agrimatch.company.mapper.CompanyMapper;
import com.agrimatch.company.service.CompanyService;
import com.agrimatch.geo.dto.GeoPoint;
import com.agrimatch.geo.service.AmapGeocodeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyMapper companyMapper;
    private final AmapGeocodeService amapGeocodeService;

    public CompanyServiceImpl(CompanyMapper companyMapper, AmapGeocodeService amapGeocodeService) {
        this.companyMapper = companyMapper;
        this.amapGeocodeService = amapGeocodeService;
    }

    @Override
    public Long create(Long ownerUserId, CompanyCreateRequest req) {
        if (ownerUserId == null) throw new ApiException(401, "未登录");
        BusCompany c = new BusCompany();
        c.setOwnerUserId(ownerUserId);
        c.setCompanyName(req.getCompanyName());
        c.setCompanyType(emptyToNull(req.getCompanyType()));
        c.setLicenseNo(req.getLicenseNo());
        c.setLicenseImgUrl(req.getLicenseImgUrl());
        c.setContacts(req.getContacts());
        c.setPhone(req.getPhone());
        c.setWechat(req.getWechat());
        c.setProvince(req.getProvince());
        c.setCity(req.getCity());
        c.setDistrict(req.getDistrict());
        c.setAddress(req.getAddress());
        c.setLocationsJson(req.getLocationsJson());
        c.setBankInfoJson(req.getBankInfoJson());

        // 自动地理编码（用户无感知）：只要有地址就自动解析坐标
        if (StringUtils.hasText(c.getAddress())) {
            try {
                GeoPoint p = amapGeocodeService.geocode(c.getAddress(), c.getCity(), c.getCompanyName());
                if (p != null) {
                    c.setLat(p.getLat());
                    c.setLng(p.getLng());
                }
            } catch (Exception ignored) {
                // 解析失败不阻塞保存，静默处理
            }
        }

        int rows = companyMapper.insert(c);
        if (rows != 1 || c.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);
        return c.getId();
    }

    @Override
    public CompanyResponse getMyCompany(Long ownerUserId) {
        if (ownerUserId == null) throw new ApiException(401, "未登录");
        BusCompany c = companyMapper.selectByOwnerUserId(ownerUserId);
        if (c == null) return null;
        return toResp(c);
    }

    @Override
    public CompanyResponse getById(Long id) {
        BusCompany c = companyMapper.selectById(id);
        if (c == null) throw new ApiException(ResultCode.NOT_FOUND);
        return toResp(c);
    }

    @Override
    public void update(Long ownerUserId, Long id, CompanyUpdateRequest req) {
        if (ownerUserId == null) throw new ApiException(401, "未登录");
        BusCompany old = companyMapper.selectById(id);
        if (old == null) throw new ApiException(ResultCode.NOT_FOUND);
        if (!ownerUserId.equals(old.getOwnerUserId())) throw new ApiException(ResultCode.NOT_FOUND);

        BusCompany c = new BusCompany();
        c.setId(id);
        c.setOwnerUserId(ownerUserId);
        c.setCompanyName(emptyToNull(req.getCompanyName()));
        c.setCompanyType(emptyToNull(req.getCompanyType()));
        c.setLicenseNo(emptyToNull(req.getLicenseNo()));
        c.setLicenseImgUrl(emptyToNull(req.getLicenseImgUrl()));
        c.setContacts(emptyToNull(req.getContacts()));
        c.setPhone(emptyToNull(req.getPhone()));
        c.setWechat(emptyToNull(req.getWechat()));
        c.setProvince(emptyToNull(req.getProvince()));
        c.setCity(emptyToNull(req.getCity()));
        c.setDistrict(emptyToNull(req.getDistrict()));
        c.setAddress(emptyToNull(req.getAddress()));
        c.setLocationsJson(emptyToNull(req.getLocationsJson()));
        c.setBankInfoJson(emptyToNull(req.getBankInfoJson()));

        // 自动地理编码逻辑（用户无感知）
        // 1. 获取最终地址（本次提交的 > 旧数据的）
        String finalAddress = c.getAddress() != null ? c.getAddress() : old.getAddress();
        String finalCity = c.getCity() != null ? c.getCity() : old.getCity();
        String finalName = c.getCompanyName() != null ? c.getCompanyName() : old.getCompanyName();
        
        // 2. 判断地址是否变更
        boolean addressChanged = c.getAddress() != null && !c.getAddress().equals(old.getAddress());
        
        // 3. 判断当前是否缺少坐标
        boolean missingCoords = old.getLat() == null || old.getLng() == null;
        
        // 4. 地址变更 或 缺少坐标时，自动解析
        if (StringUtils.hasText(finalAddress) && (addressChanged || missingCoords)) {
            try {
                GeoPoint p = amapGeocodeService.geocode(finalAddress, finalCity, finalName);
                if (p != null) {
                    c.setLat(p.getLat());
                    c.setLng(p.getLng());
                }
            } catch (Exception ignored) {
                // 解析失败不阻塞保存，静默处理
            }
        }

        int rows = companyMapper.update(c);
        if (rows != 1) throw new ApiException(ResultCode.NOT_FOUND);
    }

    /**
     * 触发一次“按地址地理编码”并写回公司坐标（用于手动补全历史数据）
     */
    public CompanyResponse geocodeAndUpdate(Long ownerUserId, Long id) {
        if (ownerUserId == null) throw new ApiException(401, "未登录");
        BusCompany c = companyMapper.selectById(id);
        if (c == null || !ownerUserId.equals(c.getOwnerUserId())) throw new ApiException(ResultCode.NOT_FOUND);
        if (!StringUtils.hasText(c.getAddress())) throw new ApiException(400, "请先填写公司地址");
        GeoPoint p = amapGeocodeService.geocode(c.getAddress(), c.getCity(), c.getCompanyName());
        if (p == null) {
            throw new ApiException(400, "无法解析坐标：请填写更完整地址（建议包含省/市/区/镇），并确认后端已配置 AMAP_WEB_KEY（Web服务Key）且已开通“地理编码/POI检索”服务");
        }

        BusCompany u = new BusCompany();
        u.setId(id);
        u.setOwnerUserId(ownerUserId);
        u.setLat(p.getLat());
        u.setLng(p.getLng());
        int rows = companyMapper.update(u);
        if (rows != 1) throw new ApiException(ResultCode.SERVER_ERROR);
        BusCompany out = companyMapper.selectById(id);
        return toResp(out);
    }

    @Override
    public void updateLicenseUrl(Long ownerUserId, Long id, String licenseImgUrl) {
        CompanyUpdateRequest req = new CompanyUpdateRequest();
        req.setLicenseImgUrl(licenseImgUrl);
        update(ownerUserId, id, req);
    }

    @Override
    public List<CompanyBriefResponse> search(String keyword, Integer limit) {
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        if (kw == null) {
            throw new ApiException(ResultCode.PARAM_ERROR);
        }
        int lim = (limit == null ? 20 : Math.max(1, Math.min(limit, 50)));
        return companyMapper.search(kw, lim);
    }

    private static CompanyResponse toResp(BusCompany c) {
        CompanyResponse r = new CompanyResponse();
        r.setId(c.getId());
        r.setOwnerUserId(c.getOwnerUserId());
        r.setCompanyName(c.getCompanyName());
        r.setCompanyType(c.getCompanyType());
        r.setLicenseNo(c.getLicenseNo());
        r.setLicenseImgUrl(c.getLicenseImgUrl());
        r.setContacts(c.getContacts());
        r.setPhone(c.getPhone());
        r.setWechat(c.getWechat());
        r.setProvince(c.getProvince());
        r.setCity(c.getCity());
        r.setDistrict(c.getDistrict());
        r.setAddress(c.getAddress());
        r.setLat(c.getLat());
        r.setLng(c.getLng());
        r.setLocationsJson(c.getLocationsJson());
        r.setBankInfoJson(c.getBankInfoJson());
        r.setCreateTime(c.getCreateTime());
        r.setUpdateTime(c.getUpdateTime());
        return r;
    }

    private static String emptyToNull(String s) {
        return StringUtils.hasText(s) ? s : null;
    }
}


