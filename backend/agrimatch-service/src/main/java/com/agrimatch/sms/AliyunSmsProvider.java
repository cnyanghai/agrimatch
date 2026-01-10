package com.agrimatch.sms;

import com.agrimatch.common.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 阿里云短信服务提供商
 * 
 * 配置项（application.yml）：
 * agrimatch:
 *   sms:
 *     provider: aliyun
 *     aliyun:
 *       access-key-id: your-access-key-id
 *       access-key-secret: your-access-key-secret
 *       sign-name: 农汇通
 *       template-code: SMS_123456789
 * 
 * 接入步骤：
 * 1. 登录阿里云控制台，开通短信服务
 * 2. 申请短信签名（如"农汇通"）
 * 3. 申请短信模板，模板变量使用 ${code}
 * 4. 创建 AccessKey 并填入配置
 * 5. 添加 Maven 依赖：
 *    <dependency>
 *      <groupId>com.aliyun</groupId>
 *      <artifactId>dysmsapi20170525</artifactId>
 *      <version>2.0.24</version>
 *    </dependency>
 */
public class AliyunSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsProvider.class);

    private final String accessKeyId;
    private final String accessKeySecret;
    private final String signName;
    private final String templateCode;

    public AliyunSmsProvider(String accessKeyId, String accessKeySecret, 
                             String signName, String templateCode) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.templateCode = templateCode;
    }

    @Override
    public void sendCode(String phone, String code, int type) {
        // TODO: 接入阿里云短信 SDK
        // 示例代码（需要添加依赖后启用）：
        /*
        try {
            com.aliyun.dysmsapi20170525.Client client = createClient();
            SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam("{\"code\":\"" + code + "\"}");
            SendSmsResponse response = client.sendSms(request);
            if (!"OK".equals(response.getBody().getCode())) {
                log.error("阿里云短信发送失败: {}", response.getBody().getMessage());
                throw new ApiException(500, "短信发送失败");
            }
            log.info("阿里云短信发送成功: phone={}, code={}", phone, code);
        } catch (Exception e) {
            log.error("阿里云短信发送异常", e);
            throw new ApiException(500, "短信发送失败");
        }
        */
        
        log.warn("阿里云短信服务尚未配置，请完成以下步骤：");
        log.warn("1. 添加 Maven 依赖 dysmsapi20170525");
        log.warn("2. 填写 access-key-id, access-key-secret, sign-name, template-code");
        log.warn("3. 取消本类中 sendCode 方法的注释代码");
        throw new ApiException(503, "短信服务暂未开通，请联系管理员");
    }

    @Override
    public String getProviderName() {
        return "aliyun";
    }
}

