package com.agrimatch.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信服务配置属性
 * 
 * 配置示例（application.yml）：
 * agrimatch:
 *   sms:
 *     provider: console  # console | aliyun | tencent
 *     aliyun:
 *       access-key-id: xxx
 *       access-key-secret: xxx
 *       sign-name: 农汇通
 *       template-code: SMS_xxx
 *     tencent:
 *       secret-id: xxx
 *       secret-key: xxx
 *       app-id: xxx
 *       sign-name: 农汇通
 *       template-id: xxx
 */
@ConfigurationProperties(prefix = "agrimatch.sms")
public class SmsProperties {

    /**
     * 短信服务提供商：console | aliyun | tencent
     * 默认为 console（开发模式，打印到控制台）
     */
    private String provider = "console";

    /**
     * 阿里云短信配置
     */
    private AliyunConfig aliyun = new AliyunConfig();

    /**
     * 腾讯云短信配置
     */
    private TencentConfig tencent = new TencentConfig();

    // Getters and Setters

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public AliyunConfig getAliyun() {
        return aliyun;
    }

    public void setAliyun(AliyunConfig aliyun) {
        this.aliyun = aliyun;
    }

    public TencentConfig getTencent() {
        return tencent;
    }

    public void setTencent(TencentConfig tencent) {
        this.tencent = tencent;
    }

    /**
     * 阿里云短信配置
     */
    public static class AliyunConfig {
        private String accessKeyId;
        private String accessKeySecret;
        private String signName;
        private String templateCode;

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getSignName() {
            return signName;
        }

        public void setSignName(String signName) {
            this.signName = signName;
        }

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }
    }

    /**
     * 腾讯云短信配置
     */
    public static class TencentConfig {
        private String secretId;
        private String secretKey;
        private String appId;
        private String signName;
        private String templateId;

        public String getSecretId() {
            return secretId;
        }

        public void setSecretId(String secretId) {
            this.secretId = secretId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getSignName() {
            return signName;
        }

        public void setSignName(String signName) {
            this.signName = signName;
        }

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }
    }
}

