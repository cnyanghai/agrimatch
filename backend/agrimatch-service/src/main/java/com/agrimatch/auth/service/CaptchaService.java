package com.agrimatch.auth.service;

import com.agrimatch.common.exception.ApiException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图形验证码服务
 */
@Service
public class CaptchaService {
    
    // 验证码存储：key -> {code, expireTime}
    private final Map<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();
    
    // 验证码有效期（5分钟）
    private static final long EXPIRE_MS = 5 * 60 * 1000;
    
    // 验证码字符集（去掉易混淆字符 0O1lI）
    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    
    private final Random random = new Random();
    
    /**
     * 生成验证码
     * @return {captchaKey, captchaImage (base64)}
     */
    public CaptchaResult generate() {
        // 清理过期验证码
        cleanExpired();
        
        // 生成验证码文本
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        String codeStr = code.toString();
        
        // 生成唯一标识
        String key = UUID.randomUUID().toString().replace("-", "");
        
        // 存储验证码
        captchaStore.put(key, new CaptchaEntry(codeStr, System.currentTimeMillis() + EXPIRE_MS));
        
        // 生成图片
        String imageBase64 = generateImage(codeStr);
        
        return new CaptchaResult(key, imageBase64);
    }
    
    /**
     * 验证验证码
     * @param key 验证码标识
     * @param code 用户输入的验证码
     * @return true 验证通过
     */
    public boolean validate(String key, String code) {
        if (key == null || code == null) return false;
        
        CaptchaEntry entry = captchaStore.get(key);
        if (entry == null) return false;
        
        // 验证后删除（一次性使用）
        captchaStore.remove(key);
        
        // 检查是否过期
        if (System.currentTimeMillis() > entry.expireTime) {
            return false;
        }
        
        // 验证码不区分大小写
        return entry.code.equalsIgnoreCase(code.trim());
    }
    
    /**
     * 验证验证码，失败抛异常
     */
    public void validateOrThrow(String key, String code) {
        if (!validate(key, code)) {
            throw new ApiException(400, "验证码错误或已过期");
        }
    }
    
    private String generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充背景（渐变色）
        GradientPaint gradient = new GradientPaint(0, 0, new Color(245, 250, 245), WIDTH, HEIGHT, new Color(230, 245, 230));
        g.setPaint(gradient);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制干扰线
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(200 + random.nextInt(55), 200 + random.nextInt(55), 200 + random.nextInt(55)));
            g.setStroke(new BasicStroke(1.5f));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 绘制干扰点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(150 + random.nextInt(100), 150 + random.nextInt(100), 150 + random.nextInt(100)));
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.fillOval(x, y, 2, 2);
        }
        
        // 绘制验证码字符
        g.setFont(new Font("Arial", Font.BOLD, 28));
        int charWidth = (WIDTH - 20) / CODE_LENGTH;
        for (int i = 0; i < code.length(); i++) {
            // 使用绿色系渐变
            g.setColor(new Color(5 + random.nextInt(50), 100 + random.nextInt(80), 50 + random.nextInt(50)));
            
            // 旋转角度
            double theta = (random.nextDouble() - 0.5) * 0.4;
            int x = 10 + i * charWidth;
            int y = 28 + random.nextInt(6);
            
            g.rotate(theta, x + charWidth / 2.0, y - 10);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-theta, x + charWidth / 2.0, y - 10);
        }
        
        g.dispose();
        
        // 转换为 Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }
    
    private void cleanExpired() {
        long now = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(entry -> now > entry.getValue().expireTime);
    }
    
    private static class CaptchaEntry {
        final String code;
        final long expireTime;
        
        CaptchaEntry(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }
    
    public static class CaptchaResult {
        private final String captchaKey;
        private final String captchaImage;
        
        public CaptchaResult(String captchaKey, String captchaImage) {
            this.captchaKey = captchaKey;
            this.captchaImage = captchaImage;
        }
        
        public String getCaptchaKey() {
            return captchaKey;
        }
        
        public String getCaptchaImage() {
            return captchaImage;
        }
    }
}

