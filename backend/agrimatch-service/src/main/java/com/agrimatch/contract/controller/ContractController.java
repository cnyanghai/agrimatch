package com.agrimatch.contract.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.contract.dto.*;
import com.agrimatch.contract.service.ContractService;
import com.agrimatch.util.NoUtil;
import com.agrimatch.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@Validated
public class ContractController {
    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/next-no")
    public Result<String> nextNo(Authentication authentication) {
        SecurityUtil.requireUserId(authentication);
        return Result.success(NoUtil.gen("HT"));
    }

    @PostMapping
    public Result<Long> create(Authentication authentication, @Valid @RequestBody ContractCreateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(contractService.create(userId, req));
    }

    /**
     * 从已确认的报价单创建合同草稿
     */
    @PostMapping("/from-quote")
    public Result<Long> createFromQuote(Authentication authentication, @Valid @RequestBody ContractFromQuoteRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(contractService.createFromQuote(userId, req));
    }

    @GetMapping("/{id}")
    public Result<ContractResponse> getById(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(contractService.getById(userId, id));
    }

    @GetMapping
    public Result<List<ContractResponse>> list(Authentication authentication, ContractQuery query) {
        Long userId = SecurityUtil.requireUserId(authentication);
        return Result.success(contractService.list(userId, query));
    }

    @PutMapping("/{id}")
    public Result<Void> update(Authentication authentication,
                              @PathVariable("id") @NotNull Long id,
                              @Valid @RequestBody ContractUpdateRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        contractService.update(userId, id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        contractService.delete(userId, id);
        return Result.success();
    }

    /**
     * 发送合同给对方签署
     */
    @PostMapping("/{id}/send")
    public Result<Void> sendForSigning(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        contractService.sendForSigning(userId, id);
        return Result.success();
    }

    /**
     * 签署合同
     */
    @PostMapping("/{id}/sign")
    public Result<Void> sign(Authentication authentication, 
                             @PathVariable("id") @NotNull Long id,
                             @Valid @RequestBody ContractSignRequest req) {
        Long userId = SecurityUtil.requireUserId(authentication);
        contractService.sign(userId, id, req);
        return Result.success();
    }

    /**
     * 下载 PDF（MVP：后端生成简单 PDF，响应头附带存证 hash）
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(Authentication authentication, @PathVariable("id") @NotNull Long id) {
        Long userId = SecurityUtil.requireUserId(authentication);
        byte[] bytes = contractService.generatePdf(userId, id);

        String fileName = "contract-" + id + ".pdf";
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        String hash = "SHA256:" + sha256Hex(bytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .header("X-Contract-Hash", hash)
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    private static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}


