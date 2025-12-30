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
}


