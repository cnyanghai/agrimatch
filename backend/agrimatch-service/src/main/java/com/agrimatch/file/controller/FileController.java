package com.agrimatch.file.controller;

import com.agrimatch.common.api.Result;
import com.agrimatch.file.dto.FileUploadResponse;
import com.agrimatch.file.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileController {
    
    private final FileStorageService fileStorageService;
    
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    /**
     * 上传图片（聊天用）
     */
    @PostMapping("/upload/image")
    public Result<FileUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        if (authentication == null) {
            return Result.fail(401, "请先登录");
        }
        
        try {
            FileStorageService.FileInfo info = fileStorageService.saveImage(file);
            FileUploadResponse response = new FileUploadResponse(
                info.getFileId(),
                info.getFileName(),
                info.getFileUrl(),
                info.getSize(),
                info.getMimeType(),
                "IMAGE"
            );
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传附件（聊天用）
     */
    @PostMapping("/upload/attachment")
    public Result<FileUploadResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        if (authentication == null) {
            return Result.fail(401, "请先登录");
        }
        
        try {
            FileStorageService.FileInfo info = fileStorageService.saveAttachment(file);
            FileUploadResponse response = new FileUploadResponse(
                info.getFileId(),
                info.getFileName(),
                info.getFileUrl(),
                info.getSize(),
                info.getMimeType(),
                "ATTACHMENT"
            );
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载/访问文件
     * 支持路径格式: /api/files/download?path=/uploads/chat/images/2025-01/xxx.jpg
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("path") String filePath,
            HttpServletRequest request
    ) {
        try {
            Resource resource = fileStorageService.loadAsResource(filePath);
            
            // 尝试确定内容类型
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                // 忽略
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // 获取文件名用于下载
            String fileName = resource.getFilename();
            if (fileName == null) {
                fileName = "file";
            }
            
            // 如果是图片，直接显示；否则作为附件下载
            boolean isImage = contentType.startsWith("image/");
            
            HttpHeaders headers = new HttpHeaders();
            if (!isImage) {
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
            }
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

