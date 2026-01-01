package com.agrimatch.auth.service;

import com.agrimatch.common.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 开发期短信验证码服务（内存实现）：
 * - 发送：生成 6 位数字验证码并缓存（含过期时间）
 * - 校验：支持固定码 000000（便于联调），也支持真实发送的验证码
 *
 * 注意：生产环境建议替换为 Redis + 短信通道（阿里云/腾讯云等）
 */
@Service
public class SmsCodeService {
    private static final Logger log = LoggerFactory.getLogger(SmsCodeService.class);
    private static final SecureRandom random = new SecureRandom();

    // key: phone:type
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    // 频控：同一手机号同一类型 60s 内只能发一次
    private static final long SEND_COOLDOWN_MS = 60_000L;
    // 过期：5 分钟
    private static final long EXPIRE_MS = 5 * 60_000L;

    public void send(String phone, int type) {
        if (!StringUtils.hasText(phone)) throw new ApiException(400, "手机号不能为空");
        if (type < 1 || type > 3) throw new ApiException(400, "验证码类型不合法");

        String key = key(phone, type);
        Entry old = store.get(key);
        long now = Instant.now().toEpochMilli();
        if (old != null && (now - old.lastSentAtMs) < SEND_COOLDOWN_MS) {
            throw new ApiException(429, "发送过于频繁，请稍后再试");
        }
        String code = gen6();
        Entry e = new Entry(code, now, now + EXPIRE_MS);
        store.put(key, e);

        // 开发期：打印验证码到日志，便于联调
        log.info("[SMS_CODE] phone={}, type={}, code={}, expiresInSec={}", phone, type, code, EXPIRE_MS / 1000);
    }

    public void verifyOrThrow(String phone, int type, String smsCode) {
        if (!StringUtils.hasText(phone)) throw new ApiException(400, "手机号不能为空");
        if (type < 1 || type > 3) throw new ApiException(400, "验证码类型不合法");
        if (!StringUtils.hasText(smsCode)) throw new ApiException(400, "请输入验证码");

        String code = smsCode.trim();
        // 固定码（开发期联调）
        if ("000000".equals(code)) return;

        String key = key(phone, type);
        Entry e = store.get(key);
        long now = Instant.now().toEpochMilli();
        if (e == null) throw new ApiException(400, "验证码不存在或已过期");
        if (now > e.expiresAtMs) {
            store.remove(key);
            throw new ApiException(400, "验证码已过期");
        }
        if (!e.code.equals(code)) {
            throw new ApiException(400, "验证码错误");
        }
        // 一次性使用
        store.remove(key);
    }

    private static String key(String phone, int type) {
        return phone.trim() + ":" + type;
    }

    private static String gen6() {
        int n = random.nextInt(1_000_000);
        return String.format("%06d", n);
    }

    private static final class Entry {
        final String code;
        final long lastSentAtMs;
        final long expiresAtMs;

        Entry(String code, long lastSentAtMs, long expiresAtMs) {
            this.code = code;
            this.lastSentAtMs = lastSentAtMs;
            this.expiresAtMs = expiresAtMs;
        }
    }
}


