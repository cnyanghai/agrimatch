package com.agrimatch.file.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        try {
            Files.createDirectories(uploadDir);
            String original = file.getOriginalFilename();
            String ext = "";
            if (StringUtils.hasText(original) && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String name = stamp + "-" + UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = uploadDir.resolve(name).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + name;
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }
}


