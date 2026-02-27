package com.agrimatch.sms;

import com.agrimatch.common.exception.ApiException;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 阿里云短信服务提供商
 */
public class AliyunSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsProvider.class);

    private final Client client;
    private final String signName;
    private final String templateCode;

    public AliyunSmsProvider(String accessKeyId, String accessKeySecret,
                             String signName, String templateCode) {
        this.signName = signName;
        this.templateCode = templateCode;
        this.client = createClient(accessKeyId, accessKeySecret);
    }

    private Client createClient(String accessKeyId, String accessKeySecret) {
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint("dysmsapi.aliyuncs.com");
            return new Client(config);
        } catch (Exception e) {
            log.error("阿里云短信客户端初始化失败", e);
            throw new RuntimeException("阿里云短信客户端初始化失败", e);
        }
    }

    @Override
    public void sendCode(String phone, String code, int type) {
        try {
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");
            SendSmsResponse response = client.sendSms(request);
            if (!"OK".equals(response.getBody().getCode())) {
                log.error("阿里云短信发送失败: phone={}, errCode={}, errMsg={}",
                        phone, response.getBody().getCode(), response.getBody().getMessage());
                throw new ApiException(500, "短信发送失败: " + response.getBody().getMessage());
            }
            log.info("阿里云短信发送成功: phone={}, type={}", phone, type);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("阿里云短信发送异常: phone={}", phone, e);
            throw new ApiException(500, "短信发送失败，请稍后重试");
        }
    }

    @Override
    public String getProviderName() {
        return "aliyun";
    }
}
