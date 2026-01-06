package com.agrimatch.file.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path uploadDir;
    
    // 允许的图片类型
    private static final List<String> IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    
    // 允许的附件类型
    private static final List<String> ATTACHMENT_TYPES = Arrays.asList(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/zip",
        "application/x-rar-compressed",
        "text/plain"
    );
    
    // 图片最大 10MB
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    // 附件最大 50MB
    private static final long MAX_ATTACHMENT_SIZE = 50 * 1024 * 1024;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * 保存文件（通用方法）
     */
    public String save(MultipartFile file) {
        return save(file, "general");
    }
    
    /**
     * 保存文件到指定子目录
     * @param file 文件
     * @param subDir 子目录（如 chat, supply 等）
     * @return 文件访问路径
     */
    public String save(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        try {
            // 按月份分目录: uploads/{subDir}/2025-01/xxx.jpg
            String monthDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Path targetDir = uploadDir.resolve(subDir).resolve(monthDir);
            Files.createDirectories(targetDir);
            
            String original = file.getOriginalFilename();
            String ext = "";
            if (StringUtils.hasText(original) && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            
            String fileId = UUID.randomUUID().toString().replace("-", "");
            String name = fileId + ext;
            Path target = targetDir.resolve(name).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回相对路径: /uploads/{subDir}/{yyyy-MM}/{name}
            return "/uploads/" + subDir + "/" + monthDir + "/" + name;
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }
    
    /**
     * 保存聊天图片
     */
    public FileInfo saveImage(MultipartFile file) {
        validateImage(file);
        String url = save(file, "chat/images");
        return new FileInfo(
            extractFileId(url),
            file.getOriginalFilename(),
            url,
            file.getSize(),
            file.getContentType()
        );
    }
    
    /**
     * 保存聊天附件
     */
    public FileInfo saveAttachment(MultipartFile file) {
        validateAttachment(file);
        String url = save(file, "chat/files");
        return new FileInfo(
            extractFileId(url),
            file.getOriginalFilename(),
            url,
            file.getSize(),
            file.getContentType()
        );
    }
    
    /**
     * 获取文件资源
     */
    public Resource loadAsResource(String filePath) {
        try {
            // 去掉开头的 /uploads/
            String relativePath = filePath.startsWith("/uploads/") 
                ? filePath.substring("/uploads/".length()) 
                : filePath;
            Path file = uploadDir.resolve(relativePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("文件不存在或不可读: " + filePath);
        } catch (MalformedURLException e) {
            throw new RuntimeException("文件路径无效: " + filePath, e);
        }
    }
    
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("图片不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("图片大小不能超过 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的图片格式，仅支持 JPG/PNG/GIF/WEBP");
        }
    }
    
    private void validateAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("附件不能为空");
        }
        if (file.getSize() > MAX_ATTACHMENT_SIZE) {
            throw new IllegalArgumentException("附件大小不能超过 50MB");
        }
        // 附件类型放宽限制，也允许图片类型
        String contentType = file.getContentType();
        if (contentType != null && !ATTACHMENT_TYPES.contains(contentType.toLowerCase()) 
                && !IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件格式");
        }
    }
    
    private String extractFileId(String url) {
        // 从 /uploads/chat/images/2025-01/abc123.jpg 提取 abc123
        int lastSlash = url.lastIndexOf('/');
        int lastDot = url.lastIndexOf('.');
        if (lastSlash >= 0 && lastDot > lastSlash) {
            return url.substring(lastSlash + 1, lastDot);
        }
        return url.substring(lastSlash + 1);
    }
    
    /**
     * 文件信息
     */
    public static class FileInfo {
        private final String fileId;
        private final String fileName;
        private final String fileUrl;
        private final long size;
        private final String mimeType;
        
        public FileInfo(String fileId, String fileName, String fileUrl, long size, String mimeType) {
            this.fileId = fileId;
            this.fileName = fileName;
            this.fileUrl = fileUrl;
            this.size = size;
            this.mimeType = mimeType;
        }
        
        public String getFileId() { return fileId; }
        public String getFileName() { return fileName; }
        public String getFileUrl() { return fileUrl; }
        public long getSize() { return size; }
        public String getMimeType() { return mimeType; }
    }
}


