package com.agrimatch.points.payment;

import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RSAPublicKeyNotificationConfig;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "wxpay.api-v3-key")
public class WxPayConfig {

    private static final Logger log = LoggerFactory.getLogger(WxPayConfig.class);

    @Bean
    public RSAPublicKeyConfig wxPayRSAConfig(WxPayProperties props) {
        String mchIdMasked = props.getMchId() != null && props.getMchId().length() > 4
                ? "****" + props.getMchId().substring(props.getMchId().length() - 4)
                : "****";
        log.info("初始化微信支付配置(公钥模式), 商户号: {}", mchIdMasked);

        return new RSAPublicKeyConfig.Builder()
                .merchantId(props.getMchId())
                .privateKeyFromPath(props.getPrivateKeyPath())
                .merchantSerialNumber(props.getCertSerialNo())
                .publicKeyFromPath(props.getPublicKeyPath())
                .publicKeyId(props.getPublicKeyId())
                .apiV3Key(props.getApiV3Key())
                .build();
    }

    @Bean
    public NativePayService nativePayService(RSAPublicKeyConfig config) {
        return new NativePayService.Builder().config(config).build();
    }

    @Bean
    public H5Service h5Service(RSAPublicKeyConfig config) {
        return new H5Service.Builder().config(config).build();
    }

    @Bean
    public JsapiServiceExtension jsapiServiceExtension(RSAPublicKeyConfig config) {
        return new JsapiServiceExtension.Builder().config(config).build();
    }

    @Bean
    public NotificationParser notificationParser(WxPayProperties props) {
        NotificationConfig notificationConfig = new RSAPublicKeyNotificationConfig.Builder()
                .publicKeyFromPath(props.getPublicKeyPath())
                .publicKeyId(props.getPublicKeyId())
                .apiV3Key(props.getApiV3Key())
                .build();
        return new NotificationParser(notificationConfig);
    }
}
