package com.agrimatch.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 开发模式短信提供商
 * 验证码打印到控制台，便于开发调试
 */
public class ConsoleSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(ConsoleSmsProvider.class);

    private static final String[] TYPE_NAMES = {"", "注册", "登录", "重置密码"};

    @Override
    public void sendCode(String phone, String code, int type) {
        String typeName = (type >= 1 && type <= 3) ? TYPE_NAMES[type] : "未知";
        log.info("┌─────────────────────────────────────────────────┐");
        log.info("│  [开发模式] 短信验证码                              │");
        log.info("│  手机号: {}                                     │", phone);
        log.info("│  验证码: {}                                     │", code);
        log.info("│  类  型: {} ({})                                │", typeName, type);
        log.info("│  有效期: 5 分钟                                   │");
        log.info("└─────────────────────────────────────────────────┘");
    }

    @Override
    public String getProviderName() {
        return "console";
    }
}

