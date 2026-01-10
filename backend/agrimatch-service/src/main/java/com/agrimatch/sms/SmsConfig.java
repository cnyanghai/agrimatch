package com.agrimatch.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信服务配置类
 * 根据配置自动选择短信提供商
 */
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsConfig {

    private static final Logger log = LoggerFactory.getLogger(SmsConfig.class);

    @Bean
    public SmsProvider smsProvider(SmsProperties props) {
        String provider = props.getProvider();
        log.info("初始化短信服务提供商: {}", provider);

        switch (provider.toLowerCase()) {
            case "aliyun":
                SmsProperties.AliyunConfig aliyun = props.getAliyun();
                log.info("使用阿里云短信服务, signName={}", aliyun.getSignName());
                return new AliyunSmsProvider(
                    aliyun.getAccessKeyId(),
                    aliyun.getAccessKeySecret(),
                    aliyun.getSignName(),
                    aliyun.getTemplateCode()
                );

            case "tencent":
                SmsProperties.TencentConfig tencent = props.getTencent();
                log.info("使用腾讯云短信服务, signName={}", tencent.getSignName());
                return new TencentSmsProvider(
                    tencent.getSecretId(),
                    tencent.getSecretKey(),
                    tencent.getAppId(),
                    tencent.getSignName(),
                    tencent.getTemplateId()
                );

            case "console":
            default:
                log.info("使用开发模式短信服务（验证码将打印到控制台）");
                return new ConsoleSmsProvider();
        }
    }
}

