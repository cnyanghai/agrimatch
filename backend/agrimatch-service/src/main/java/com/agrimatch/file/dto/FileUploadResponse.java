package com.agrimatch.file.dto;

public class FileUploadResponse {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private long size;
    private String mimeType;
    private String fileType; // IMAGE 或 ATTACHMENT
    
    public FileUploadResponse() {}
    
    public FileUploadResponse(String fileId, String fileName, String fileUrl, long size, String mimeType, String fileType) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.size = size;
        this.mimeType = mimeType;
        this.fileType = fileType;
    }
    
    public String getFileId() {
        return fileId;
    }
    
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}

