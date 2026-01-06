package com.agrimatch.contract.dto;

/**
 * 合同签署请求
 */
public class ContractSignRequest {
    private String signType;        // seal=盖章, handwrite=手写签名, typed=打字签名, seal_handwrite=盖章+签名
    private Long sealId;            // 使用的印章ID（如果选择盖章）
    private String signatureData;   // 手写签名图片数据（Base64）
    private String typedName;       // 打字签名的名字
    private String signerName;      // 签署人姓名
    private String signerTitle;     // 签署人职位

    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }

    public Long getSealId() { return sealId; }
    public void setSealId(Long sealId) { this.sealId = sealId; }

    public String getSignatureData() { return signatureData; }
    public void setSignatureData(String signatureData) { this.signatureData = signatureData; }

    public String getTypedName() { return typedName; }
    public void setTypedName(String typedName) { this.typedName = typedName; }

    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }

    public String getSignerTitle() { return signerTitle; }
    public void setSignerTitle(String signerTitle) { this.signerTitle = signerTitle; }
}

