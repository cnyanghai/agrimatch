package com.agrimatch.company.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.company.dto.CompanyCreateRequest;
import com.agrimatch.company.dto.CompanyBriefResponse;
import com.agrimatch.company.dto.CompanyResponse;
import com.agrimatch.company.dto.CompanyUpdateRequest;
import com.agrimatch.company.service.CompanyService;
import com.agrimatch.file.service.FileStorageService;
import com.agrimatch.security.LoginUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@Validated
public class CompanyController {
    private final CompanyService companyService;
    private final FileStorageService fileStorageService;

    public CompanyController(CompanyService companyService, FileStorageService fileStorageService) {
        this.companyService = companyService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public Result<Long> create(Authentication authentication, @Valid @RequestBody CompanyCreateRequest req) {
        Long userId = requireUserId(authentication);
        return Result.success(companyService.create(userId, req));
    }

    @GetMapping("/my")
    public Result<CompanyResponse> my(Authentication authentication) {
        Long userId = requireUserId(authentication);
        return Result.success(companyService.getMyCompany(userId));
    }

    @GetMapping("/{id}")
    public Result<CompanyResponse> getById(@PathVariable("id") @NotNull Long id) {
        return Result.success(companyService.getById(id));
    }

    @GetMapping("/by-user/{userId}")
    public Result<CompanyResponse> getByUserId(@PathVariable("userId") @NotNull Long userId) {
        return Result.success(companyService.getMyCompany(userId));
    }

    @GetMapping("/search")
    public Result<List<CompanyBriefResponse>> search(Authentication authentication,
                                                     @RequestParam("keyword") String keyword,
                                                     @RequestParam(value = "limit", required = false) Integer limit) {
        requireUserId(authentication);
        return Result.success(companyService.search(keyword, limit));
    }

    @GetMapping("/suppliers")
    public Result<List<com.agrimatch.company.dto.CompanyCardResponse>> suppliers(@RequestParam(value = "limit", required = false) Integer limit,
                                                                               @RequestParam(value = "region", required = false) String region) {
        return Result.success(companyService.getTopSuppliers(limit, region));
    }

    @GetMapping("/buyers")
    public Result<List<com.agrimatch.company.dto.CompanyCardResponse>> buyers(@RequestParam(value = "limit", required = false) Integer limit,
                                                                            @RequestParam(value = "categoryName", required = false) String categoryName) {
        return Result.success(companyService.getTopBuyers(limit, categoryName));
    }

    @GetMapping("/top")
    public Result<List<com.agrimatch.company.dto.CompanyCardResponse>> top(@RequestParam("type") String type,
                                                                         @RequestParam(value = "limit", required = false) Integer limit) {
        return Result.success(companyService.getTopCompanies(type, limit));
    }

    @GetMapping("/directory")
    public Result<com.agrimatch.common.api.PageResult<com.agrimatch.company.dto.CompanyCardResponse>> directory(
            @RequestParam("type") String type,
            @RequestParam(value = "letter", required = false) String letter,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(companyService.getDirectory(type, letter, page, size));
    }

    @GetMapping("/{id}/profile")
    public Result<com.agrimatch.company.dto.CompanyProfileResponse> profile(@PathVariable("id") Long id) {
        return Result.success(companyService.getProfile(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(Authentication authentication,
                               @PathVariable("id") @NotNull Long id,
                               @Valid @RequestBody CompanyUpdateRequest req) {
        Long userId = requireUserId(authentication);
        companyService.update(userId, id, req);
        return Result.success();
    }

    @PostMapping("/{id}/license")
    public Result<String> uploadLicense(Authentication authentication,
                                        @PathVariable("id") @NotNull Long id,
                                        @RequestParam("file") MultipartFile file) {
        Long userId = requireUserId(authentication);
        String url = fileStorageService.save(file);
        companyService.updateLicenseUrl(userId, id, url);
        return Result.success(url);
    }

    @PostMapping("/{id}/geocode")
    public Result<CompanyResponse> geocode(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = requireUserId(authentication);
        return Result.success(companyService.geocodeAndUpdate(userId, id));
    }

    private static Long requireUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new com.agrimatch.common.exception.ApiException(401, "未登录");
        }
        return ((LoginUser) authentication.getPrincipal()).getUserId();
    }
}


