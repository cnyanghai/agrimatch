package com.agrimatch.company.service;

import com.agrimatch.company.dto.CompanyCreateRequest;
import com.agrimatch.company.dto.CompanyBriefResponse;
import com.agrimatch.company.dto.CompanyResponse;
import com.agrimatch.company.dto.CompanyUpdateRequest;

import java.util.List;

public interface CompanyService {
    Long create(Long ownerUserId, CompanyCreateRequest req);

    CompanyResponse getMyCompany(Long ownerUserId);

    CompanyResponse getById(Long id);

    void update(Long ownerUserId, Long id, CompanyUpdateRequest req);

    void updateLicenseUrl(Long ownerUserId, Long id, String licenseImgUrl);

    CompanyResponse geocodeAndUpdate(Long ownerUserId, Long id);

    List<CompanyBriefResponse> search(String keyword, Integer limit);

    List<com.agrimatch.company.dto.CompanyCardResponse> getTopSuppliers(Integer limit, String region);

    List<com.agrimatch.company.dto.CompanyCardResponse> getTopBuyers(Integer limit, String categoryName);

    List<com.agrimatch.company.dto.CompanyCardResponse> getTopCompanies(String type, Integer limit);

    com.agrimatch.common.api.PageResult<com.agrimatch.company.dto.CompanyCardResponse> getDirectory(String type, String letter, int page, int size);

    com.agrimatch.company.dto.CompanyProfileResponse getProfile(Long id);
}


