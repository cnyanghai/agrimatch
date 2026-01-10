package com.agrimatch.sms;

import com.agrimatch.common.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 腾讯云短信服务提供商
 * 
 * 配置项（application.yml）：
 * agrimatch:
 *   sms:
 *     provider: tencent
 *     tencent:
 *       secret-id: your-secret-id
 *       secret-key: your-secret-key
 *       app-id: 1400000000
 *       sign-name: 农汇通
 *       template-id: 123456
 * 
 * 接入步骤：
 * 1. 登录腾讯云控制台，开通短信服务
 * 2. 创建应用获取 SDK AppID
 * 3. 申请短信签名（如"农汇通"）
 * 4. 申请短信模板，模板变量使用 {1}
 * 5. 创建 API 密钥并填入配置
 * 6. 添加 Maven 依赖：
 *    <dependency>
 *      <groupId>com.tencentcloudapi</groupId>
 *      <artifactId>tencentcloud-sdk-java-sms</artifactId>
 *      <version>3.1.900</version>
 *    </dependency>
 */
public class TencentSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(TencentSmsProvider.class);

    private final String secretId;
    private final String secretKey;
    private final String appId;
    private final String signName;
    private final String templateId;

    public TencentSmsProvider(String secretId, String secretKey, 
                              String appId, String signName, String templateId) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.appId = appId;
        this.signName = signName;
        this.templateId = templateId;
    }

    @Override
    public void sendCode(String phone, String code, int type) {
        // TODO: 接入腾讯云短信 SDK
        // 示例代码（需要添加依赖后启用）：
        /*
        try {
            Credential cred = new Credential(secretId, secretKey);
            SmsClient client = new SmsClient(cred, "ap-guangzhou");
            
            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(appId);
            req.setSignName(signName);
            req.setTemplateId(templateId);
            req.setPhoneNumberSet(new String[]{"+86" + phone});
            req.setTemplateParamSet(new String[]{code});
            
            SendSmsResponse resp = client.SendSms(req);
            SendStatus status = resp.getSendStatusSet()[0];
            if (!"Ok".equals(status.getCode())) {
                log.error("腾讯云短信发送失败: {}", status.getMessage());
                throw new ApiException(500, "短信发送失败");
            }
            log.info("腾讯云短信发送成功: phone={}, code={}", phone, code);
        } catch (Exception e) {
            log.error("腾讯云短信发送异常", e);
            throw new ApiException(500, "短信发送失败");
        }
        */
        
        log.warn("腾讯云短信服务尚未配置，请完成以下步骤：");
        log.warn("1. 添加 Maven 依赖 tencentcloud-sdk-java-sms");
        log.warn("2. 填写 secret-id, secret-key, app-id, sign-name, template-id");
        log.warn("3. 取消本类中 sendCode 方法的注释代码");
        throw new ApiException(503, "短信服务暂未开通，请联系管理员");
    }

    @Override
    public String getProviderName() {
        return "tencent";
    }
}

