package com.agrimatch.sms;

/**
 * 短信发送服务提供商接口
 * 
 * 实现类：
 * - ConsoleSmsProvider: 开发模式，打印到控制台
 * - AliyunSmsProvider: 阿里云短信服务
 * - TencentSmsProvider: 腾讯云短信服务
 */
public interface SmsProvider {

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @param type  验证码类型：1=注册, 2=登录, 3=重置密码
     */
    void sendCode(String phone, String code, int type);

    /**
     * 获取提供商名称
     */
    String getProviderName();
}

