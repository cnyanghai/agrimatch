package com.agrimatch.contract.dto;

/**
 * 创建电子章请求
 */
public class SealCreateRequest {
    private String sealName;     // 章名称
    private String sealType;     // 章类型：official=公章, contract=合同章
    private String sealUrl;      // 上传的章图片URL（上传方式时使用）
    private boolean generate;    // 是否自动生成

    public String getSealName() { return sealName; }
    public void setSealName(String sealName) { this.sealName = sealName; }

    public String getSealType() { return sealType; }
    public void setSealType(String sealType) { this.sealType = sealType; }

    public String getSealUrl() { return sealUrl; }
    public void setSealUrl(String sealUrl) { this.sealUrl = sealUrl; }

    public boolean isGenerate() { return generate; }
    public void setGenerate(boolean generate) { this.generate = generate; }
}

