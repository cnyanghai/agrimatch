package com.agrimatch.points.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wxpay")
public class WxPayProperties {

    private String mchId;
    private String appId;
    private String apiV3Key;
    private String certSerialNo;
    private String privateKeyPath;
    private String notifyUrl;
    private String publicKeyPath;
    private String publicKeyId;

    public String getMchId() { return mchId; }
    public void setMchId(String mchId) { this.mchId = mchId; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getApiV3Key() { return apiV3Key; }
    public void setApiV3Key(String apiV3Key) { this.apiV3Key = apiV3Key; }

    public String getCertSerialNo() { return certSerialNo; }
    public void setCertSerialNo(String certSerialNo) { this.certSerialNo = certSerialNo; }

    public String getPrivateKeyPath() { return privateKeyPath; }
    public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }

    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }

    public String getPublicKeyPath() { return publicKeyPath; }
    public void setPublicKeyPath(String publicKeyPath) { this.publicKeyPath = publicKeyPath; }

    public String getPublicKeyId() { return publicKeyId; }
    public void setPublicKeyId(String publicKeyId) { this.publicKeyId = publicKeyId; }
}
