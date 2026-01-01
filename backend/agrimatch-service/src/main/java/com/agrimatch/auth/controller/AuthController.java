package com.agrimatch.auth.controller;

import com.agrimatch.auth.dto.LoginRequest;
import com.agrimatch.auth.dto.LoginBySmsRequest;
import com.agrimatch.auth.dto.LoginResponse;
import com.agrimatch.auth.dto.MeResponse;
import com.agrimatch.auth.dto.RegisterRequest;
import com.agrimatch.auth.dto.SmsSendRequest;
import com.agrimatch.auth.service.AuthService;
import com.agrimatch.auth.service.SmsCodeService;
import com.agrimatch.common.api.Result;
import com.agrimatch.security.LoginUser;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String TOKEN_COOKIE = "agrimatch_token";

    private final AuthService authService;
    private final SmsCodeService smsCodeService;
    private final long jwtExpireMs;

    public AuthController(AuthService authService,
                          SmsCodeService smsCodeService,
                          @Value("${security.jwt.expire-ms:604800000}") long jwtExpireMs) {
        this.authService = authService;
        this.smsCodeService = smsCodeService;
        this.jwtExpireMs = jwtExpireMs;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        LoginResponse r = authService.login(req.getUserName(), req.getPassword());
        setTokenCookie(response, r.getToken());
        return Result.success(r);
    }

    @PostMapping("/login/sms")
    public Result<LoginResponse> loginBySms(@Valid @RequestBody LoginBySmsRequest req, HttpServletResponse response) {
        // type=2 登录
        smsCodeService.verifyOrThrow(req.getPhone(), 2, req.getSmsCode());
        LoginResponse r = authService.loginByPhone(req.getPhone());
        setTokenCookie(response, r.getToken());
        return Result.success(r);
    }

    @PostMapping("/sms/send")
    public Result<Void> sendSms(@Valid @RequestBody SmsSendRequest req) {
        smsCodeService.send(req.getPhone(), req.getType());
        return Result.success();
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req, HttpServletResponse response) {
        LoginResponse r = authService.register(req);
        setTokenCookie(response, r.getToken());
        return Result.success(r);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        clearTokenCookie(response);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<MeResponse> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            return Result.fail(401, "未登录");
        }
        LoginUser p = (LoginUser) authentication.getPrincipal();
        return Result.success(authService.me(p.getUserId()));
    }

    private void setTokenCookie(HttpServletResponse response, String token) {
        if (token == null || token.isBlank()) return;
        long maxAgeSec = Math.max(0, jwtExpireMs / 1000);
        ResponseCookie cookie = ResponseCookie.from(TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(false) // 生产环境建议 true（https）
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAgeSec)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}


