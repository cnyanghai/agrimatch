package com.agrimatch.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtTokenUtil {
    private final SecretKey key;
    private final long expireMs;

    public JwtTokenUtil(String secret, long expireMs) {
        // HMAC key should be >= 256-bit
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(bytes.length >= 32 ? bytes : pad(bytes, 32));
        this.expireMs = expireMs;
    }

    public String generateToken(Long userId, String userName) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userName", userName)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireMs))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static byte[] pad(byte[] src, int minLen) {
        byte[] out = new byte[minLen];
        System.arraycopy(src, 0, out, 0, Math.min(src.length, minLen));
        for (int i = src.length; i < minLen; i++) out[i] = (byte) 0x5a;
        return out;
    }
}


